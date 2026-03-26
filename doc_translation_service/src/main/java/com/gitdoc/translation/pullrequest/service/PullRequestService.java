package com.gitdoc.translation.pullrequest.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitdoc.translation.entity.RepositoryBranchEntity;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.repository.BranchJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.github.GitHubApiService;
import com.gitdoc.translation.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PullRequestService {

    private final GitHubApiService gitHubApiService;
    private final TaskJpaRepository taskRepository;
    private final BranchJpaRepository branchRepository;
    private final OperationLogService logService;

    public void createOrUpdatePR(TranslationTaskEntity task, RepositoryEntity repo,
                                 Map<String, String> translatedFiles) {
        String fullName = repo.getFullName();
        Long installationId = repo.getInstallationId();

        // The source branch this task is translating (null → default branch)
        String sourceBranch = task.getBranchName() != null ? task.getBranchName() : repo.getDefaultBranch();

        // Only reuse an existing open PR when it targets the SAME source branch.
        // Filtering by branchName prevents dev-branch translations from being
        // incorrectly appended to a master-branch PR (or vice versa).
        Optional<TranslationTaskEntity> existingTaskWithPR = taskRepository
                .findLatestCompletedTaskWithPRForBranch(repo.getId(), "completed", task.getBranchName());

        String branchName;
        String parentCommitSha;
        String baseTreeSha;
        // 获取当前的分支的上一个 pr 信息，并且向 github 核对上次的 pr 是否已经合入
        if (existingTaskWithPR.isPresent() && isPROpen(fullName, existingTaskWithPR.get().getPrNumber(), installationId)) {
            // Append new translations to the existing open PR for this branch ， 尚未合入，复用合入到上一个 pr 中
            branchName = existingTaskWithPR.get().getPrBranch();
            parentCommitSha = gitHubApiService.getLatestCommitSha(fullName, branchName, installationId);
            baseTreeSha = gitHubApiService.getCommitTreeSha(fullName, parentCommitSha, installationId);
            log.info("Appending to existing PR branch: {} (source: {})", branchName, sourceBranch);
        } else {
            // No open PR for this source branch — create a fresh one.
            // Use milliseconds + random suffix to guarantee uniqueness even when two
            // tasks complete within the same second (epoch-seconds alone caused 422).
            branchName = "doc-translation-" + Instant.now().toEpochMilli()
                    + "-" + UUID.randomUUID().toString().substring(0, 6);
            String sourceBranchSha = gitHubApiService.getLatestCommitSha(fullName, sourceBranch, installationId);
            baseTreeSha = gitHubApiService.getCommitTreeSha(fullName, sourceBranchSha, installationId);
            // 基于当前时间节点创建一个临时分支
            gitHubApiService.createBranch(fullName, branchName, sourceBranchSha, installationId);
            parentCommitSha = sourceBranchSha;
            log.info("Created new PR branch: {} (based on {})", branchName, sourceBranch);
        }

        // Create tree with translated files
        String newTreeSha = gitHubApiService.createTree(fullName, baseTreeSha, translatedFiles, installationId);

        // Create commit — [skip-translation] prevents webhook from re-triggering on merge
        String langs = String.join(", ", task.getTargetLanguages());
        boolean isAuto = "webhook".equals(task.getTriggerType());
        String commitMsg = "docs: add translations (" + langs + ") [skip-translation]";
        String newCommitSha = gitHubApiService.createCommit(
                fullName, commitMsg, newTreeSha, parentCommitSha, installationId); // 将修改提交到临时分支

        // Update branch 将临时分支文件引用指向最新的提交
        gitHubApiService.updateBranchRef(fullName, branchName, newCommitSha, installationId);

        // Create PR if new branch
        boolean isNewPr = existingTaskWithPR.isEmpty() || !isPROpen(fullName, existingTaskWithPR.get().getPrNumber(), installationId);
        if (isNewPr) {
            String triggerLabel = isAuto ? "[自动触发]" : "[手动]";
            // [skip-translation] in title covers squash-merge scenario
            String prTitle = "[doc-translation]" + triggerLabel + " Update translations (" + langs + ") [skip-translation]";
            String prBody = buildPRBody(task, translatedFiles, isAuto);
            // 创建一个 rp ，从 临时分支 合入到 目标分支
            JsonNode pr = gitHubApiService.createPullRequest(
                    fullName, prTitle, prBody, branchName, sourceBranch, installationId);

            int prNumber = pr.get("number").asInt();
            String prUrl = pr.get("html_url").asText();

            task.setPrNumber(prNumber);
            task.setPrUrl(prUrl);
            task.setPrBranch(branchName);
            task.setPrStatus("open");
            taskRepository.save(task);

            log.info("Created PR #{} for repo {}", prNumber, fullName);

            // Auto-merge if the branch-level option is enabled
            String taskBranch = task.getBranchName() != null ? task.getBranchName() : repo.getDefaultBranch();
            boolean autoMerge = branchRepository
                    .findByRepositoryIdAndBranchName(repo.getId(), taskBranch)
                    .map(RepositoryBranchEntity::getAutoMergePr)
                    .orElse(false);
            if (autoMerge) {
                // 是否配置了自动合入
                tryAutoMergePR(task, repo, prNumber, prTitle, installationId);
            }
        } else {
            // Update existing task with PR info
            task.setPrNumber(existingTaskWithPR.get().getPrNumber());
            task.setPrUrl(existingTaskWithPR.get().getPrUrl());
            task.setPrBranch(branchName);
            // Preserve the existing prStatus (could be "open" already)
            task.setPrStatus(existingTaskWithPR.get().getPrStatus() != null
                    ? existingTaskWithPR.get().getPrStatus() : "open");
            taskRepository.save(task);
        }
    }

    /**
     * Attempts to auto-merge the newly created PR using the squash strategy.
     * <p>If the merge succeeds the task's prStatus is updated to "merged".
     * If it fails (e.g. branch-protection rules, pending checks) the task is
     * marked as "failed" with a descriptive error message so the user is aware.
     */
    private void tryAutoMergePR(TranslationTaskEntity task, RepositoryEntity repo,
                                int prNumber, String prTitle, Long installationId) {
        try {
            gitHubApiService.mergePullRequest(repo.getFullName(), prNumber, prTitle, installationId);
            task.setPrStatus("merged");
            taskRepository.save(task);
            logService.log(repo.getUserId(), repo.getId(), "pr.auto_merged",
                    "PR #" + prNumber + " 自动合并成功");
            log.info("Auto-merged PR #{} for repo {}", prNumber, repo.getFullName());

            //  自动合入删除临时分支
            gitHubApiService.deleteBranch(repo.getFullName(), task.getPrBranch(), installationId);
            log.info("Auto-merged delete branch #{} ", task.getPrBranch());
        } catch (Exception e) {
            String reason = e.getMessage() != null ? e.getMessage() : "未知错误";
            // Keep prStatus as "open" so the user can still merge manually
            String errorMsg = "自动合并 PR #" + prNumber + " 失败（PR 仍处于开启状态，请手动合并）: " + reason;
            task.setStatus("failed");
            task.setErrorMessage(
                    task.getErrorMessage() != null
                            ? task.getErrorMessage() + " | " + errorMsg
                            : errorMsg);
            taskRepository.save(task);
            logService.log(repo.getUserId(), repo.getId(), "pr.auto_merge_failed",
                    "PR #" + prNumber + " 自动合并失败: " + reason);
            log.warn("Auto-merge failed for PR #{} in repo {}: {}", prNumber, repo.getFullName(), reason);
        }
    }

    /**
     * Checks whether the given PR is still open by fetching it directly from GitHub.
     * Querying the PR list would only return the first page and silently miss PRs
     * beyond that limit, causing false "closed" judgements.
     */
    private boolean isPROpen(String fullName, Integer prNumber, Long installationId) {
        if (prNumber == null) return false;
        try {
            JsonNode pr = gitHubApiService.getPullRequest(fullName, prNumber, installationId);
            return pr != null && "open".equals(pr.get("state").asText());
        } catch (Exception e) {
            log.warn("Failed to check PR #{} status, assuming closed: {}", prNumber, e.getMessage());
            return false;
        }
    }

    private String buildPRBody(TranslationTaskEntity task, Map<String, String> translatedFiles, boolean isAuto) {
        String fileList = translatedFiles.keySet().stream()
                .limit(20)
                .map(p -> "- `" + p + "`")
                .collect(Collectors.joining("\n"));
        if (translatedFiles.size() > 20) {
            fileList += "\n- ... and " + (translatedFiles.size() - 20) + " more files";
        }

        String heading;
        String triggerInfo;
        if (isAuto) {
            heading = "## 🤖 自动触发翻译";
            String sha = task.getTriggerCommitSha();
            triggerInfo = "- 触发方式: Webhook (Push 事件自动触发)\n" +
                    "- 触发 Commit: `" + (sha != null ? sha.substring(0, Math.min(8, sha.length())) : "N/A") + "`";
        } else {
            heading = "## 🖊️ 手动翻译";
            triggerInfo = "- 触发方式: 手动触发";
        }

        return """
                %s
                
                ### 变更摘要
                %s
                - 翻译文件数: %d
                - 目标语言: %s
                - Token 消耗: %d
                
                ### 变更文件列表
                %s
                
                ---
                *由 [GitHub Doc Translation](https://github.com/apps/github-doc-translation) 自动生成*
                """.formatted(
                heading,
                triggerInfo,
                translatedFiles.size(),
                String.join(", ", task.getTargetLanguages()),
                task.getTokensUsed(),
                fileList
        );
    }
}
