package com.gitdoc.translation.repo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.common.util.EncryptionUtil;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.TranslationFileEntity;
import com.gitdoc.translation.entity.UserEntity;
import com.gitdoc.translation.entity.repository.FileJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import com.gitdoc.translation.github.GitHubApiService;
import com.gitdoc.translation.github.GitHubTokenManager;
import com.gitdoc.translation.github.StaleInstallationException;
import com.gitdoc.translation.repo.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepoService {

    private final RepoJpaRepository repoRepository;
    private final UserJpaRepository userRepository;
    private final FileJpaRepository fileRepository;
    private final TaskJpaRepository taskRepository;
    private final GitHubApiService gitHubApiService;
    private final GitHubTokenManager tokenManager;
    private final EncryptionUtil encryptionUtil;

    public PageResponse<RepoDTO> listRepos(Long userId, int page, int size, String search, String status) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<RepositoryEntity> repos = repoRepository.findByUserIdWithFilter(userId, search, status, pageRequest);
        return PageResponse.of(repos.map(this::toDTO));
    }

    /**
     * Get GitHub-authorized repos for the user (from all their installations)
     */
    public PageResponse<AvailableRepoDTO> listAvailableRepos(Long userId, int page, int size) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));

        if (user.getGithubAccessToken() == null) {
            return new PageResponse<>(List.of(), 0, page, size, 0);
        }

        String userToken = encryptionUtil.decrypt(user.getGithubAccessToken());
        List<AvailableRepoDTO> result = new ArrayList<>();

        try {
            JsonNode installations = gitHubApiService.getUserInstallations(userToken);
            JsonNode installationList = installations.get("installations");

            if (installationList != null) {
                for (JsonNode installation : installationList) {
                    Long installationId = installation.get("id").asLong();
                    JsonNode reposResponse = gitHubApiService.getInstallationRepositories(installationId);
                    JsonNode repos = reposResponse.get("repositories");

                    if (repos != null) {
                        for (JsonNode repo : repos) {
                            AvailableRepoDTO dto = new AvailableRepoDTO();
                            dto.setGithubRepoId(repo.get("id").asLong());
                            dto.setInstallationId(installationId);
                            dto.setFullName(repo.get("full_name").asText());
                            dto.setDefaultBranch(repo.get("default_branch").asText("main"));
                            dto.setDescription(repo.has("description") && !repo.get("description").isNull()
                                    ? repo.get("description").asText() : null);
                            dto.setStarCount(repo.get("stargazers_count").asInt(0));
                            dto.setLanguage(repo.has("language") && !repo.get("language").isNull()
                                    ? repo.get("language").asText() : null);
                            dto.setIsPrivate(repo.get("private").asBoolean(false));
                            dto.setAlreadyAdded(repoRepository.existsByUserIdAndGithubRepoId(userId, dto.getGithubRepoId()));
                            result.add(dto);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch available repos for user {}", userId, e);
        }

        // Simple pagination
        int start = (page - 1) * size;
        int end = Math.min(start + size, result.size());
        List<AvailableRepoDTO> pageContent = start < result.size() ? result.subList(start, end) : List.of();
        int totalPages = (int) Math.ceil((double) result.size() / size);
        return new PageResponse<>(pageContent, result.size(), page, size, totalPages);
    }

    @Transactional
    public RepoDTO addRepo(Long userId, AddRepoRequest request) {
        if (repoRepository.existsByUserIdAndGithubRepoId(userId, request.getGithubRepoId())) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "仓库已添加到平台");
        }

        RepositoryEntity entity = new RepositoryEntity();
        entity.setUserId(userId);
        entity.setGithubRepoId(request.getGithubRepoId());
        entity.setFullName(request.getFullName());
        entity.setDescription(request.getDescription());
        entity.setDefaultBranch(request.getDefaultBranch());
        entity.setInstallationId(request.getInstallationId());

        return toDTO(repoRepository.save(entity));
    }

    public RepoDetailDTO getRepoDetail(Long userId, Long repoId) {
        RepositoryEntity repo = getAndValidateRepo(userId, repoId);

        RepoDetailDTO dto = new RepoDetailDTO();
        dto.setId(repo.getId());
        dto.setFullName(repo.getFullName());
        dto.setDefaultBranch(repo.getDefaultBranch());
        dto.setBaseLanguage(repo.getBaseLanguage());
        dto.setTargetLanguages(repo.getTargetLanguages());
        dto.setAiModel(repo.getAiModel());
        dto.setStatus(repo.getStatus());
        dto.setLastSyncAt(repo.getLastSyncAt());

        // Build stats
        RepoDetailDTO.RepoStats stats = new RepoDetailDTO.RepoStats();
        List<TranslationFileEntity> allFiles = fileRepository.findByRepositoryId(repoId);
        long completedCount = allFiles.stream().filter(f -> "completed".equals(f.getStatus())).count();
        stats.setTotalFiles(allFiles.size());
        stats.setTranslatedFiles(completedCount);
        stats.setPendingChanges(taskRepository.findActiveTasksByRepositoryId(repoId).size());
        stats.setTotalPRs(taskRepository.findTop10ByRepositoryIdOrderByCreatedAtDesc(repoId).stream()
                .filter(t -> t.getPrNumber() != null).count());

        List<RepoDetailDTO.LanguageProgress> langProgress = new ArrayList<>();
        for (String lang : repo.getTargetLanguages()) {
            long total = allFiles.stream().filter(f -> lang.equals(f.getTargetLanguage())).count();
            long completed = allFiles.stream().filter(f -> lang.equals(f.getTargetLanguage())
                    && "completed".equals(f.getStatus())).count();
            int percentage = total > 0 ? (int) (completed * 100 / total) : 0;
            RepoDetailDTO.LanguageProgress lp = new RepoDetailDTO.LanguageProgress();
            lp.setLanguage(lang);
            lp.setTotal(total);
            lp.setCompleted(completed);
            lp.setPercentage(percentage);
            langProgress.add(lp);
        }
        stats.setLanguageProgress(langProgress);
        dto.setStats(stats);

        return dto;
    }

    @Transactional
    public void removeRepo(Long userId, Long repoId) {
        RepositoryEntity repo = getAndValidateRepo(userId, repoId);
        repoRepository.delete(repo);
    }

    public List<FileTreeNode> getFileTree(Long userId, Long repoId, String branch) {
        RepositoryEntity repo = getAndValidateRepo(userId, repoId);
        String resolvedBranch = (branch != null && !branch.isBlank()) ? branch : repo.getDefaultBranch();
        Long installationId = resolveInstallationId(userId, repo);
        try {
            return fetchTree(repo, resolvedBranch, installationId);
        } catch (StaleInstallationException e) {
            log.warn("Stale installationId={} detected when loading file tree for repo {}, re-resolving...",
                    e.getInstallationId(), repo.getFullName());
            Long freshId = forceRefreshInstallationId(userId, repo);
            return fetchTree(repo, resolvedBranch, freshId);
        }
    }

    public List<String> getAvailableBranches(Long userId, Long repoId) {
        RepositoryEntity repo = getAndValidateRepo(userId, repoId);
        Long installationId = resolveInstallationId(userId, repo);
        try {
            com.fasterxml.jackson.databind.JsonNode response = gitHubApiService.listBranches(
                    repo.getFullName(), installationId);
            List<String> branches = new java.util.ArrayList<>();
            if (response != null && response.isArray()) {
                response.forEach(b -> branches.add(b.get("name").asText()));
            }
            return branches;
        } catch (Exception e) {
            log.warn("Failed to fetch branches for repo {}: {}", repo.getFullName(), e.getMessage());
            return List.of(repo.getDefaultBranch());
        }
    }

    private List<FileTreeNode> fetchTree(RepositoryEntity repo, String branch, Long installationId) {
        JsonNode treeResponse = gitHubApiService.getRepoTree(
                repo.getFullName(), branch, installationId);
        JsonNode items = treeResponse.get("tree");
        if (items == null) return List.of();
        return buildTree(items);
    }

    public RepositoryEntity getAndValidateRepo(Long userId, Long repoId) {
        RepositoryEntity repo = repoRepository.findById(repoId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "仓库不存在"));
        if (!repo.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN);
        }
        return repo;
    }

    /**
     * Returns the installationId for the repo.
     * If stored value is null, auto-resolves from user's GitHub installations and persists the result.
     */
    public Long resolveInstallationId(Long userId, RepositoryEntity repo) {
        if (repo.getInstallationId() != null) {
            return repo.getInstallationId();
        }
        return doResolveFromGitHub(userId, repo);
    }

    /**
     * Force re-resolves the installationId from GitHub, ignoring any cached/stored value.
     * Used when the stored value is detected as stale at runtime.
     */
    @Transactional
    public Long forceRefreshInstallationId(Long userId, RepositoryEntity repo) {
        log.info("Force-refreshing installationId for repo {}", repo.getFullName());
        // Evict stale token from Redis so next call fetches a fresh one
        if (repo.getInstallationId() != null) {
            tokenManager.evictInstallationToken(repo.getInstallationId());
        }
        repo.setInstallationId(null);
        return doResolveFromGitHub(userId, repo);
    }

    private Long doResolveFromGitHub(Long userId, RepositoryEntity repo) {
        log.info("Resolving installationId from GitHub for repo {}", repo.getId());
        try {
            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "用户不存在"));
            if (user.getGithubAccessToken() == null) {
                throw BusinessException.of(ErrorCode.PARAM_ERROR, "用户未授权 GitHub，请重新登录");
            }
            String userToken = encryptionUtil.decrypt(user.getGithubAccessToken());
            JsonNode installations = gitHubApiService.getUserInstallations(userToken);
            JsonNode installationList = installations.get("installations");
            if (installationList != null) {
                for (JsonNode installation : installationList) {
                    Long installationId = installation.get("id").asLong();
                    JsonNode reposResponse = gitHubApiService.getInstallationRepositories(installationId);
                    JsonNode repos = reposResponse.get("repositories");
                    if (repos != null) {
                        for (JsonNode r : repos) {
                            if (repo.getGithubRepoId() != null &&
                                    repo.getGithubRepoId().equals(r.get("id").asLong())) {
                                repo.setInstallationId(installationId);
                                repo.setStatus("active");
                                repoRepository.save(repo);
                                log.info("Resolved and persisted installationId={} for repo {}", installationId, repo.getId());
                                return installationId;
                            }
                        }
                    }
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to resolve installationId for repo {}", repo.getId(), e);
        }
        throw BusinessException.of(ErrorCode.INSTALLATION_NOT_FOUND);
    }

    private List<FileTreeNode> buildTree(JsonNode items) {
        // Build a flat map of path -> node, then nest
        Map<String, FileTreeNode> dirMap = new LinkedHashMap<>();
        List<FileTreeNode> roots = new ArrayList<>();

        // Collect directories and .md files
        for (JsonNode item : items) {
            String type = item.get("type").asText();
            String path = item.get("path").asText();

            if ("tree".equals(type)) {
                FileTreeNode dir = new FileTreeNode();
                dir.setPath(path);
                dir.setType("directory");
                dir.setChildren(new ArrayList<>());
                dirMap.put(path, dir);
            } else if ("blob".equals(type) && path.endsWith(".md")) {
                FileTreeNode file = new FileTreeNode();
                file.setPath(path);
                file.setType("file");
                file.setSize(item.has("size") ? item.get("size").asLong() : null);
                dirMap.put(path, file);
            }
        }

        // Build tree structure
        for (Map.Entry<String, FileTreeNode> entry : dirMap.entrySet()) {
            String path = entry.getKey();
            FileTreeNode node = entry.getValue();
            int lastSlash = path.lastIndexOf('/');
            if (lastSlash < 0) {
                roots.add(node);
            } else {
                String parentPath = path.substring(0, lastSlash);
                FileTreeNode parent = dirMap.get(parentPath);
                if (parent != null && parent.getChildren() != null) {
                    parent.getChildren().add(node);
                } else {
                    roots.add(node);
                }
            }
        }

        // Filter out directories that only contain non-md files (empty children)
        return roots.stream()
                .filter(n -> "file".equals(n.getType()) || (n.getChildren() != null && !n.getChildren().isEmpty()))
                .toList();
    }

    private RepoDTO toDTO(RepositoryEntity entity) {
        RepoDTO dto = new RepoDTO();
        dto.setId(entity.getId());
        dto.setFullName(entity.getFullName());
        dto.setDescription(entity.getDescription());
        dto.setDefaultBranch(entity.getDefaultBranch());
        dto.setBaseLanguage(entity.getBaseLanguage());
        dto.setTargetLanguages(entity.getTargetLanguages());
        dto.setAiModel(entity.getAiModel());
        dto.setStatus(entity.getStatus());
        dto.setLastSyncAt(entity.getLastSyncAt());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setTranslatedFileCount((int) fileRepository.countByRepositoryIdAndStatus(entity.getId(), "completed"));
        return dto;
    }
}
