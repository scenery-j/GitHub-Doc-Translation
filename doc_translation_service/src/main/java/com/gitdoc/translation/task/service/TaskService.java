package com.gitdoc.translation.task.service;

import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.entity.RepositoryBranchEntity;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.TranslationFileEntity;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.repository.BranchJpaRepository;
import com.gitdoc.translation.entity.repository.FileJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.repo.service.RepoService;
import com.gitdoc.translation.task.dto.TaskDTO;
import com.gitdoc.translation.task.dto.TaskDetailDTO;
import com.gitdoc.translation.task.dto.TriggerTranslationRequest;
import com.gitdoc.translation.task.queue.TranslationTaskQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskJpaRepository taskRepository;
    private final FileJpaRepository fileRepository;
    private final RepoJpaRepository repoRepository;
    private final BranchJpaRepository branchRepository;
    private final RepoService repoService;
    private final TranslationTaskQueue taskQueue;

    @Transactional
    public List<TaskDTO> triggerTranslation(Long userId, Long repoId, TriggerTranslationRequest request) {
        RepositoryEntity repo = repoService.getAndValidateRepo(userId, repoId);

        if (repo.getTargetLanguages().isEmpty()) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "请先配置目标翻译语言");
        }

        // Resolve branch list: use provided branches or fall back to first configured branch
        List<String> targetBranches = (request.getBranches() != null && !request.getBranches().isEmpty())
                ? request.getBranches()
                : List.of((String) null);  // null → resolveBranchConfig picks first configured

        List<TaskDTO> result = new ArrayList<>();
        for (String branchName : targetBranches) {
            RepositoryBranchEntity branchCfg = resolveBranchConfig(repoId, branchName);
            if (branchCfg.getSelectedPaths().isEmpty()) {
                log.warn("Branch {} has no selected paths, skipping", branchCfg.getBranchName());
                continue;
            }
            result.add(createTaskForBranch(repo, repoId, branchCfg));
        }

        if (result.isEmpty()) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "所有分支均未配置翻译文件，请先选择文件");
        }
        return result;
    }

    private TaskDTO createTaskForBranch(RepositoryEntity repo, Long repoId, RepositoryBranchEntity branchCfg) {
        TranslationTaskEntity task = new TranslationTaskEntity();
        task.setRepositoryId(repoId);
        task.setBranchName(branchCfg.getBranchName());
        task.setTriggerType("manual");
        task.setTargetLanguages(repo.getTargetLanguages());
        task.setStatus("queued");

        List<String> paths = branchCfg.getSelectedPaths();
        task.setTotalFiles(paths.size() * repo.getTargetLanguages().size());
        task = taskRepository.save(task);
        log.info("save task: {}", task.getId());
        for (String path : paths) {
            for (String lang : repo.getTargetLanguages()) {
                TranslationFileEntity file = new TranslationFileEntity();
                file.setTaskId(task.getId());
                file.setRepositoryId(repoId);
                file.setSourcePath(path);
                file.setTargetLanguage(lang);
                file.setTargetPath(buildTargetPath(branchCfg.getOutputPathPattern(), lang, path));
                file.setStatus("pending");
                fileRepository.save(file);
            }
        }

        enqueueAfterCommit(task.getId());
        return toDTO(task, repo.getFullName());
    }

    public PageResponse<TaskDTO> listTasks(Long userId, Long repoId, int page, int size, String status) {
        repoService.getAndValidateRepo(userId, repoId);
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<TranslationTaskEntity> tasks = taskRepository.findByRepositoryIdWithFilter(repoId, status, pageRequest);
        RepositoryEntity repo = repoRepository.findById(repoId).orElseThrow();
        return PageResponse.of(tasks.map(t -> toDTO(t, repo.getFullName())));
    }

    public TaskDetailDTO getTaskDetail(Long userId, Long taskId) {
        TranslationTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "任务不存在"));

        RepositoryEntity repo = repoRepository.findById(task.getRepositoryId()).orElseThrow();
        if (!repo.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN);
        }

        List<TranslationFileEntity> files = fileRepository.findByTaskId(taskId);

        TaskDetailDTO dto = new TaskDetailDTO();
        dto.setId(task.getId());
        dto.setRepositoryId(task.getRepositoryId());
        dto.setRepositoryName(repo.getFullName());
        dto.setBranchName(task.getBranchName());
        dto.setTriggerType(task.getTriggerType());
        dto.setStatus(task.getStatus());
        dto.setTargetLanguages(task.getTargetLanguages());
        dto.setTotalFiles(task.getTotalFiles());
        dto.setCompletedFiles(task.getCompletedFiles());
        dto.setFailedFiles(task.getFailedFiles());
        dto.setTokensUsed(task.getTokensUsed());
        dto.setEstimatedCost(task.getEstimatedCost());
        dto.setPrNumber(task.getPrNumber());
        dto.setPrUrl(task.getPrUrl());
        dto.setPrStatus(task.getPrStatus());
        dto.setErrorMessage(task.getErrorMessage());
        dto.setStartedAt(task.getStartedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setFiles(files.stream().map(this::toFileDTO).toList());
        return dto;
    }

    @Transactional
    public void cancelTask(Long userId, Long taskId) {
        TranslationTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));

        RepositoryEntity repo = repoRepository.findById(task.getRepositoryId()).orElseThrow();
        if (!repo.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.FORBIDDEN);
        }

        if (!List.of("queued", "running").contains(task.getStatus())) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "只有排队中或进行中的任务可以取消");
        }

        task.setStatus("cancelled");
        taskRepository.save(task);
    }

    /**
     * Create task from webhook (called by WebhookService).
     *
     * @param changedFiles map of {@code filepath → unified-diff patch}.
     *                     The patch is {@code null} for newly added files.
     *                     It is stored on each {@link TranslationFileEntity} so the
     *                     executor can later ask the AI for the optimal translation strategy.
     */
    @Transactional
    public TranslationTaskEntity createWebhookTask(RepositoryEntity repo,
                                                   RepositoryBranchEntity branchCfg,
                                                   Map<String, String> changedFiles,
                                                   String commitSha) {
        TranslationTaskEntity task = new TranslationTaskEntity();
        task.setRepositoryId(repo.getId());
        task.setBranchName(branchCfg.getBranchName());
        task.setTriggerType("webhook");
        task.setTriggerCommitSha(commitSha);
        task.setTargetLanguages(repo.getTargetLanguages());
        task.setStatus("queued");
        task.setTotalFiles(changedFiles.size() * repo.getTargetLanguages().size());
        task = taskRepository.save(task);

        for (Map.Entry<String, String> entry : changedFiles.entrySet()) {
            String path = entry.getKey();
            String patch = entry.getValue();   // null for added files
            for (String lang : repo.getTargetLanguages()) {
                TranslationFileEntity file = new TranslationFileEntity();
                file.setTaskId(task.getId());
                file.setRepositoryId(repo.getId());
                file.setSourcePath(path);
                file.setTargetLanguage(lang);
                file.setTargetPath(buildTargetPath(branchCfg.getOutputPathPattern(), lang, path));
                file.setPatch(patch);
                file.setStatus("pending");
                fileRepository.save(file);
            }
        }

        enqueueAfterCommit(task.getId());
        return task;
    }

    /**
     * Resolve branch config: use requested branch, or fall back to first configured branch.
     */
    private RepositoryBranchEntity resolveBranchConfig(Long repoId, String requestedBranch) {
        if (requestedBranch != null && !requestedBranch.isBlank()) {
            return branchRepository.findByRepositoryIdAndBranchName(repoId, requestedBranch)
                    .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND,
                            "分支配置不存在: " + requestedBranch + "，请先在翻译配置中添加该分支"));
        }
        List<RepositoryBranchEntity> branches = branchRepository.findByRepositoryId(repoId);
        if (branches.isEmpty()) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "请先添加分支翻译配置");
        }
        return branches.get(0);
    }

    /**
     * Defers enqueue until after the current transaction commits.
     * This prevents the consumer thread from querying a task record that has been
     * written but not yet visible (transaction not committed).
     * Falls back to immediate enqueue when called outside a transaction context.
     */
    private void enqueueAfterCommit(Long taskId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            taskQueue.enqueue(taskId);
                        }
                    }
            );
        } else {
            taskQueue.enqueue(taskId);
        }
    }

    private String buildTargetPath(String pattern, String lang, String sourcePath) {
        String base = pattern.replace("{lang}", lang);
        if (!base.endsWith("/")) base += "/";
        return base + sourcePath;
    }

    private TaskDTO toDTO(TranslationTaskEntity task, String repoName) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setRepositoryName(repoName);
        dto.setBranchName(task.getBranchName());
        dto.setTriggerType(task.getTriggerType());
        dto.setStatus(task.getStatus());
        dto.setTargetLanguages(task.getTargetLanguages());
        dto.setTotalFiles(task.getTotalFiles());
        dto.setCompletedFiles(task.getCompletedFiles());
        dto.setFailedFiles(task.getFailedFiles());
        dto.setTokensUsed(task.getTokensUsed());
        dto.setEstimatedCost(task.getEstimatedCost());
        dto.setPrNumber(task.getPrNumber());
        dto.setPrUrl(task.getPrUrl());
        dto.setPrStatus(task.getPrStatus());
        dto.setStartedAt(task.getStartedAt());
        dto.setCompletedAt(task.getCompletedAt());
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    private TaskDetailDTO.FileProgressDTO toFileDTO(TranslationFileEntity file) {
        TaskDetailDTO.FileProgressDTO dto = new TaskDetailDTO.FileProgressDTO();
        dto.setId(file.getId());
        dto.setSourcePath(file.getSourcePath());
        dto.setTargetLanguage(file.getTargetLanguage());
        dto.setTargetPath(file.getTargetPath());
        dto.setStatus(file.getStatus());
        dto.setTokensUsed(file.getTokensUsed());
        dto.setErrorMessage(file.getErrorMessage());
        dto.setTranslatedAt(file.getTranslatedAt());
        return dto;
    }
}
