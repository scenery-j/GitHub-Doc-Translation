package com.gitdoc.translation.sync.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdoc.translation.entity.RepositoryBranchEntity;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.UserEntity;
import com.gitdoc.translation.entity.repository.BranchJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import com.gitdoc.translation.github.GitHubApiService;
import com.gitdoc.translation.github.GitHubTokenManager;
import com.gitdoc.translation.log.service.OperationLogService;
import com.gitdoc.translation.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebhookService {

    private final RepoJpaRepository repoRepository;
    private final BranchJpaRepository branchRepository;
    private final UserJpaRepository userRepository;
    private final TaskJpaRepository taskRepository;
    private final GitHubApiService gitHubApiService;
    private final GitHubTokenManager tokenManager;
    private final TaskService taskService;
    private final OperationLogService logService;
    private final ObjectMapper objectMapper;

    @Async
    public void processPushEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);

            String repoFullName = event.get("repository").get("full_name").asText();
            String ref = event.get("ref").asText();
            String before = event.get("before").asText();
            String after = event.get("after").asText();

            // Find repository in platform
            Optional<RepositoryEntity> repoOpt = repoRepository.findByFullName(repoFullName);
            if (repoOpt.isEmpty()) {
                log.debug("Repository {} not registered on platform, skipping", repoFullName);
                return;
            }

            RepositoryEntity repo = repoOpt.get();

            if (!"active".equals(repo.getStatus())) {
                log.debug("Repository {} is not active, skipping", repoFullName);
                return;
            }

            // Find branch config for the pushed branch
            String pushedBranch = ref.startsWith("refs/heads/") ? ref.substring("refs/heads/".length()) : ref;
            Optional<RepositoryBranchEntity> branchOpt =
                    branchRepository.findByRepositoryIdAndBranchName(repo.getId(), pushedBranch);

            if (branchOpt.isEmpty()) {
                log.info("Branch {} not configured for translation in repo {}, skipping", pushedBranch, repoFullName);
                return;
            }

            RepositoryBranchEntity branchCfg = branchOpt.get();
            if (!Boolean.TRUE.equals(branchCfg.getWebhookActive())) {
                log.debug("Auto-trigger is disabled for branch {} in repo {}, skipping", pushedBranch, repoFullName);
                return;
            }

            // Skip commits made by this translation tool to avoid recursive loops.
            if (isTranslationToolPush(event)) {
                log.debug("Skipping push event from translation tool for repo {}", repoFullName);
                return;
            }

            logService.log(repo.getUserId(), repo.getId(), "webhook.received",
                    "收到 push 事件 [" + pushedBranch + "], commit: " + after.substring(0, Math.min(8, after.length())));

            // Get changed files with their diffs filtered by this branch's selected paths
            // Map: filepath → unified-diff patch (null for newly added files)
            Map<String, String> changedFiles = getChangedMarkdownFiles(repo, branchCfg, before, after);

            if (changedFiles.isEmpty()) {
                log.info("No relevant markdown changes detected for repo {} branch {}", repoFullName, pushedBranch);
                return;
            }

            log.info("Detected {} changed files in repo {} branch {}", changedFiles.size(), repoFullName, pushedBranch);

            taskService.createWebhookTask(repo, branchCfg, changedFiles, after);

        } catch (Exception e) {
            log.error("Failed to process push event", e);
        }
    }

    /**
     * Handles `installation` webhook events.
     * <ul>
     *   <li>deleted / suspend  → evict token cache, clear installation_id, mark repos as error</li>
     *   <li>created / unsuspend → resolve new installation_id and restore affected repos</li>
     * </ul>
     */
    @Async
    @Transactional
    public void processInstallationEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String action = event.get("action").asText();
            JsonNode installation = event.get("installation");
            Long installationId = installation.get("id").asLong();
            log.info("GitHub App installation event: action={}, installationId={}", action, installationId);

            switch (action) {
                case "deleted", "suspend" -> handleInstallationRemoved(installationId);
                case "created", "unsuspend" -> handleInstallationCreated(installation, event);
                default -> log.debug("Unhandled installation action: {}", action);
            }
        } catch (Exception e) {
            log.error("Failed to process installation event", e);
        }
    }

    /**
     * Handles `installation_repositories` webhook events.
     * <ul>
     *   <li>removed → clear installation_id, mark repos as error</li>
     *   <li>added   → update installation_id for matching repos</li>
     * </ul>
     */
    @Async
    @Transactional
    public void processInstallationRepositoriesEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String action = event.get("action").asText();
            JsonNode installation = event.get("installation");
            Long installationId = installation.get("id").asLong();
            Long githubAccountId = installation.get("account").get("id").asLong();
            log.info("GitHub App installation_repositories event: action={}, installationId={}", action, installationId);

            Optional<UserEntity> userOpt = userRepository.findByGithubId(githubAccountId);
            if (userOpt.isEmpty()) {
                log.debug("No platform user found for GitHub account {}, skipping", githubAccountId);
                return;
            }
            Long userId = userOpt.get().getId();

            switch (action) {
                case "removed" -> {
                    JsonNode removed = event.get("repositories_removed");
                    if (removed != null) {
                        for (JsonNode repo : removed) {
                            Long githubRepoId = repo.get("id").asLong();
                            repoRepository.findByUserIdAndGithubRepoId(userId, githubRepoId)
                                    .ifPresent(r -> {
                                        log.info("Repo {} lost installation access, marking as error", r.getFullName());
                                        r.setInstallationId(null);
                                        r.setStatus("error");
                                        repoRepository.save(r);
                                    });
                        }
                    }
                }
                case "added" -> {
                    JsonNode added = event.get("repositories_added");
                    if (added != null) {
                        for (JsonNode repo : added) {
                            Long githubRepoId = repo.get("id").asLong();
                            repoRepository.findByUserIdAndGithubRepoId(userId, githubRepoId)
                                    .ifPresent(r -> {
                                        log.info("Repo {} regained installation access, restoring installationId={}",
                                                r.getFullName(), installationId);
                                        r.setInstallationId(installationId);
                                        r.setStatus("active");
                                        repoRepository.save(r);
                                    });
                        }
                    }
                }
                default -> log.debug("Unhandled installation_repositories action: {}", action);
            }
        } catch (Exception e) {
            log.error("Failed to process installation_repositories event", e);
        }
    }

    // ── Installation helpers ──────────────────────────────────────────────────

    /**
     * App uninstalled or suspended: evict token cache + mark all repos as error
     */
    private void handleInstallationRemoved(Long installationId) {
        tokenManager.evictInstallationToken(installationId);
        List<RepositoryEntity> repos = repoRepository.findAllByInstallationId(installationId);
        if (repos.isEmpty()) {
            log.debug("No repos found for installationId={}", installationId);
            return;
        }
        repos.forEach(r -> {
            log.info("Clearing stale installationId for repo {} due to uninstall/suspend", r.getFullName());
            r.setInstallationId(null);
            r.setStatus("error");
        });
        repoRepository.saveAll(repos);
        log.info("Marked {} repos as error after installation {} removed", repos.size(), installationId);
    }

    /**
     * App installed or unsuspended: find the user, then update installation_id
     * for all repos that belong to this new installation.
     */
    private void handleInstallationCreated(JsonNode installation, JsonNode event) {
        Long newInstallationId = installation.get("id").asLong();
        Long githubAccountId = installation.get("account").get("id").asLong();

        Optional<UserEntity> userOpt = userRepository.findByGithubId(githubAccountId);
        if (userOpt.isEmpty()) {
            log.debug("No platform user found for GitHub account {}, skipping installation.created", githubAccountId);
            return;
        }
        Long userId = userOpt.get().getId();

        // Collect github_repo_ids available under the new installation
        Set<Long> installationRepoIds = resolveInstallationRepoIds(newInstallationId, event);
        if (installationRepoIds.isEmpty()) {
            log.warn("Could not determine repos for new installationId={}", newInstallationId);
            return;
        }

        // Update all user's repos that are in this installation
        List<RepositoryEntity> userRepos = repoRepository.findByUserId(userId,
                org.springframework.data.domain.Pageable.unpaged()).getContent();

        int updated = 0;
        for (RepositoryEntity repo : userRepos) {
            if (repo.getGithubRepoId() != null && installationRepoIds.contains(repo.getGithubRepoId())) {
                log.info("Restoring installationId={} for repo {}", newInstallationId, repo.getFullName());
                repo.setInstallationId(newInstallationId);
                repo.setStatus("active");
                repoRepository.save(repo);
                updated++;
            }
        }
        log.info("Updated installationId for {} repos after installation {} created", updated, newInstallationId);
    }

    /**
     * Returns the set of GitHub repo IDs accessible under the given installation.
     * First tries the payload's `repositories` field (available when user chooses specific repos),
     * then falls back to calling the GitHub API.
     */
    private Set<Long> resolveInstallationRepoIds(Long installationId, JsonNode event) {
        // Try payload first
        JsonNode reposInPayload = event.get("repositories");
        if (reposInPayload != null && reposInPayload.isArray() && reposInPayload.size() > 0) {
            return StreamSupport.stream(reposInPayload.spliterator(), false)
                    .map(r -> r.get("id").asLong())
                    .collect(Collectors.toSet());
        }
        // Fall back to GitHub API
        try {
            JsonNode response = gitHubApiService.getInstallationRepositories(installationId);
            JsonNode repos = response.get("repositories");
            if (repos != null) {
                return StreamSupport.stream(repos.spliterator(), false)
                        .map(r -> r.get("id").asLong())
                        .collect(Collectors.toSet());
            }
        } catch (Exception e) {
            log.error("Failed to fetch repos for installationId={}", installationId, e);
        }
        return Set.of();
    }

    @Async
    public void processPullRequestEvent(String payload) {
        try {
            JsonNode event = objectMapper.readTree(payload);
            String action = event.get("action").asText();
            String repoFullName = event.get("repository").get("full_name").asText();
            int prNumber = event.get("number").asInt();

            if ("closed".equals(action)) {
                boolean merged = event.get("pull_request").get("merged").asBoolean(false);
                String newPrStatus = merged ? "merged" : "closed";
                String logMsg = merged ? "PR #" + prNumber + " 已合并" : "PR #" + prNumber + " 已关闭";
                logService.logByRepoName(repoFullName, merged ? "pr.merged" : "pr.closed", logMsg);

                // Update prStatus on all tasks that reference this PR
                repoRepository.findByFullName(repoFullName).ifPresent(repo -> {
                    List<TranslationTaskEntity> tasks =
                            taskRepository.findByRepositoryIdAndPrNumber(repo.getId(), prNumber);
                    if (!tasks.isEmpty()) {
                        tasks.forEach(t -> t.setPrStatus(newPrStatus));
                        taskRepository.saveAll(tasks);
                        log.info("Updated prStatus={} for {} task(s) linked to PR #{} in {}",
                                newPrStatus, tasks.size(), prNumber, repoFullName);
                    }
                });
            }
        } catch (Exception e) {
            log.error("Failed to process pull_request event", e);
        }
    }

    /**
     * Returns a map of {@code filepath → unified-diff patch} for every relevant
     * Markdown file that changed between {@code before} and {@code after}.
     *
     * <ul>
     *   <li>The patch is {@code null} for newly added files (no previous version to diff against).</li>
     *   <li>Removed files and files outside the configured paths are excluded.</li>
     * </ul>
     */
    private Map<String, String> getChangedMarkdownFiles(RepositoryEntity repo,
                                                        RepositoryBranchEntity branchCfg,
                                                        String before, String after) {
        try {
            JsonNode compareResponse = gitHubApiService.compareCommits(
                    repo.getFullName(), before, after, repo.getInstallationId());

            JsonNode files = compareResponse.get("files");
            if (files == null) {
                return Map.of();
            }

            Map<String, String> changedFiles = new java.util.LinkedHashMap<>();
            String outputPrefix = branchCfg.getOutputPathPattern().replace("{lang}", "").replaceAll("//+", "/");

            for (JsonNode file : files) {
                String filename = file.get("filename").asText();
                String status = file.get("status").asText();

                if (!filename.endsWith(".md")) continue;
                if (filename.startsWith(outputPrefix)) continue;
                if (!isInSelectedPaths(filename, branchCfg.getSelectedPaths())) continue;
                if ("removed".equals(status)) continue;

                // "added" files have no previous version → patch is null (triggers full translation)
                // "modified" / "renamed" files have a patch with the exact diff
                String patch = file.has("patch") && !file.get("patch").isNull()
                        ? file.get("patch").asText() : null;

                changedFiles.put(filename, patch);
            }

            return changedFiles;
        } catch (Exception e) {
            log.error("Failed to compare commits for repo {}", repo.getFullName(), e);
            return Map.of();
        }
    }

    private boolean isInSelectedPaths(String filePath, List<String> selectedPaths) {
        if (selectedPaths == null || selectedPaths.isEmpty()) return true;
        for (String selected : selectedPaths) {
            if (filePath.equals(selected) || filePath.startsWith(selected + "/")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true when the push event was produced by merging one of this tool's
     * translation PRs. The tool stamps every commit with {@code [skip-translation]}
     * so we can reliably detect it regardless of the merge strategy used:
     * <ul>
     *   <li>Regular merge – individual PR commits appear in {@code commits[]}</li>
     *   <li>Squash merge  – the squashed commit message equals the PR title which also
     *       carries {@code [skip-translation]}</li>
     *   <li>Rebase merge  – each commit is replayed and appears in {@code commits[]}</li>
     * </ul>
     */
    private boolean isTranslationToolPush(JsonNode event) {
        // Check head_commit (covers squash-merge where squashed msg == PR title)
        JsonNode headCommit = event.get("head_commit");
        if (headCommit != null && headCommit.get("message").asText("").contains("[skip-translation]")) {
            return true;
        }
        // Check individual commits (covers regular & rebase merge)
        JsonNode commits = event.get("commits");
        if (commits != null) {
            for (JsonNode commit : commits) {
                if (commit.get("message").asText("").contains("[skip-translation]")) {
                    return true;
                }
            }
        }
        return false;
    }
}
