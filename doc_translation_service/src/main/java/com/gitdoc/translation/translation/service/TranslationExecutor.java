package com.gitdoc.translation.translation.service;

import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.TranslationFileEntity;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.repository.FileJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import com.gitdoc.translation.github.GitHubApiService;
import com.gitdoc.translation.log.service.OperationLogService;
import com.gitdoc.translation.pullrequest.service.PullRequestService;
import com.gitdoc.translation.translation.markdown.MarkdownProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationExecutor {

    private final TaskJpaRepository taskRepository;
    private final RepoJpaRepository repoRepository;
    private final FileJpaRepository fileRepository;
    private final UserJpaRepository userRepository;
    private final GitHubApiService gitHubApiService;
    private final TranslationAIService translationAIService;
    private final PullRequestService pullRequestService;
    private final OperationLogService logService;
    private final TranslationLinkInjector linkInjector;
    private final MarkdownProcessor markdownProcessor;

    @Value("${app.translation.max-concurrent:10}")
    private int maxConcurrent;

    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public void execute(Long taskId) {
        TranslationTaskEntity task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        if ("cancelled".equals(task.getStatus())) {
            return;
        }

        RepositoryEntity repo = repoRepository.findById(task.getRepositoryId())
                .orElseThrow(() -> new RuntimeException("Repository not found: " + task.getRepositoryId()));

        // Get user ID from repository
        Long userId = repo.getUserId();

        // Update task status to running
        task.setStatus("running");
        task.setStartedAt(LocalDateTime.now());
        taskRepository.save(task);

        logService.log(userId, repo.getId(), "translation.started",
                "翻译任务开始 #" + taskId + "，目标语言: " + task.getTargetLanguages());

        try {
            // Get all pending files
            List<TranslationFileEntity> files = fileRepository.findByTaskId(taskId);
            if (files.isEmpty()) {
                completeTask(task, repo, userId, Map.of());
                return;
            }

            // Execute translations concurrently
            Semaphore semaphore = new Semaphore(maxConcurrent);
            Map<String, String> translatedFiles = Collections.synchronizedMap(new LinkedHashMap<>());
            // Capture effectively final copies for lambda
            final RepositoryEntity repoFinal = repo;
            final Long userIdFinal = userId;

            final TranslationTaskEntity taskFinal = task;
            List<CompletableFuture<Void>> futures = files.stream()
                    .map(file -> CompletableFuture.runAsync(() -> {
                        try {
                            semaphore.acquire();
                            translateSingleFile(file, repoFinal, taskFinal, userIdFinal, translatedFiles);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            semaphore.release();
                        }
                    }, virtualThreadExecutor))
                    .toList();

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            // Inject translation links into source files before creating PR
            injectTranslationLinksToSources(repo, files, translatedFiles, task.getBranchName());

            // Reload task to get latest stats
            TranslationTaskEntity refreshedTask = taskRepository.findById(taskId).orElse(task);
            completeTask(refreshedTask, repo, userId, translatedFiles);

        } catch (Exception e) {
            log.error("Translation task {} failed", taskId, e);
            task.setStatus("failed");
            task.setErrorMessage(e.getMessage());
            task.setCompletedAt(LocalDateTime.now());
            taskRepository.save(task);
            logService.log(userId, repo.getId(), "translation.failed", "任务 #" + taskId + " 失败: " + e.getMessage());
        }
    }

    private void translateSingleFile(TranslationFileEntity file, RepositoryEntity repo,
                                     TranslationTaskEntity task, Long userId,
                                     Map<String, String> translatedFiles) {
        file.setStatus("translating");
        fileRepository.save(file);

        try {
            String newContent = gitHubApiService.getFileContent(
                    repo.getFullName(), file.getSourcePath(), repo.getInstallationId(), task.getBranchName());

            file.setSourceHash(sha256(newContent));

            String projectName = repo.getFullName().substring(repo.getFullName().indexOf('/') + 1);
            TranslationAIService.TranslationResult result;

            // Use patch-based incremental translation only for webhook tasks that:
            //   1. Have a diff patch (modified files — not newly added ones)
            //   2. Already have a previous completed translation to update
            String patch = file.getPatch();
            boolean hasHistory = fileRepository
                    .findFirstByRepositoryIdAndSourcePathAndTargetLanguageAndStatusOrderByCreatedAtDesc(
                            repo.getId(), file.getSourcePath(), file.getTargetLanguage(), "completed")
                    .isPresent();

            if (patch != null && !patch.isBlank() && hasHistory) {
                result = translateWithPatch(file, repo, task, userId, projectName, newContent, patch);
            } else {
                result = translationAIService.translate(
                        newContent, repo.getBaseLanguage(), file.getTargetLanguage(),
                        projectName, file.getSourcePath(), userId, repo.getAiModel());
            }

            file.setStatus("completed");
            file.setTokensUsed(result.tokensUsed());
            file.setTranslatedAt(LocalDateTime.now());
            fileRepository.save(file);

            translatedFiles.put(file.getTargetPath(), result.content());
            updateTaskProgress(file.getTaskId(), true, result.tokensUsed());

        } catch (Exception e) {
            log.error("Failed to translate file: {}", file.getSourcePath(), e);
            file.setStatus("failed");
            file.setErrorMessage(e.getMessage().substring(0, Math.min(500, e.getMessage().length())));
            fileRepository.save(file);
            updateTaskProgress(file.getTaskId(), false, 0);
        }
    }

    /**
     * Patch-based incremental translation:
     *
     * <ol>
     *   <li>Send the git diff {@code patch} to the AI to decide: full vs. incremental.</li>
     *   <li>If the AI recommends <em>incremental</em>, it also tells us which section
     *       headings (e.g. {@code "## Installation"}) need re-translation.</li>
     *   <li>We locate those sections by heading in the new source, translate each one,
     *       then replace them at the same position in the existing translation.</li>
     *   <li>On any failure or structural mismatch we fall back to full translation.</li>
     * </ol>
     */
    private TranslationAIService.TranslationResult translateWithPatch(
            TranslationFileEntity file, RepositoryEntity repo, TranslationTaskEntity task,
            Long userId, String projectName, String newContent, String patch) {

        try {
            // Ask AI for strategy decision
            TranslationAIService.IncrementalStrategy strategy =
                    translationAIService.analyzeIncrementalStrategy(patch, repo.getAiModel(), userId);

            if (strategy.isFull()) {
                log.info("[incremental] AI recommended full translation for {}", file.getSourcePath());
                return translationAIService.translate(newContent, repo.getBaseLanguage(),
                        file.getTargetLanguage(), projectName, file.getSourcePath(),
                        userId, repo.getAiModel());
            }

            // --- Incremental path ---
            List<String> newSections = markdownProcessor.splitIntoSections(newContent);
            log.info("[incremental] AI recommended incremental for {} — sections to update: {}",
                    file.getSourcePath(), strategy.sections());

            // Map AI-provided headings to section indices in the new source
            List<Integer> targetIndices = resolveHeadingIndices(strategy.sections(), newSections);
            if (targetIndices.isEmpty()) {
                log.warn("[incremental] Could not resolve any section headings for {}, falling back to full",
                        file.getSourcePath());
                return translationAIService.translate(newContent, repo.getBaseLanguage(),
                        file.getTargetLanguage(), projectName, file.getSourcePath(),
                        userId, repo.getAiModel());
            }

            // Fetch the existing translation and split it into sections
            String existingTranslation = fetchExistingTranslation(repo, file, task.getBranchName());
            if (existingTranslation.isBlank()) {
                return translationAIService.translate(newContent, repo.getBaseLanguage(),
                        file.getTargetLanguage(), projectName, file.getSourcePath(),
                        userId, repo.getAiModel());
            }

            List<String> translatedSections = new ArrayList<>(
                    markdownProcessor.splitIntoSections(existingTranslation));

            if (translatedSections.size() != newSections.size()) {
                // Structural change (sections added/removed) — fall back to full
                log.warn("[incremental] Section count mismatch ({} vs {}) for {}, falling back to full",
                        newSections.size(), translatedSections.size(), file.getSourcePath());
                return translationAIService.translate(newContent, repo.getBaseLanguage(),
                        file.getTargetLanguage(), projectName, file.getSourcePath(),
                        userId, repo.getAiModel());
            }

            // Translate only the identified sections and splice them in
            long totalTokens = 0;
            for (int idx : targetIndices) {
                TranslationAIService.TranslationResult sectionResult = translationAIService.translate(
                        newSections.get(idx), repo.getBaseLanguage(), file.getTargetLanguage(),
                        projectName, file.getSourcePath() + " [section " + idx + "]",
                        userId, repo.getAiModel());
                translatedSections.set(idx, sectionResult.content());
                totalTokens += sectionResult.tokensUsed();
            }

            log.info("[incremental] Re-translated {}/{} sections for {}",
                    targetIndices.size(), newSections.size(), file.getSourcePath());
            return new TranslationAIService.TranslationResult(
                    markdownProcessor.joinSections(translatedSections), totalTokens);

        } catch (Exception e) {
            log.warn("[incremental] Patch-based translation failed for {}, falling back to full: {}",
                    file.getSourcePath(), e.getMessage());
            return translationAIService.translate(newContent, repo.getBaseLanguage(),
                    file.getTargetLanguage(), projectName, file.getSourcePath(),
                    userId, repo.getAiModel());
        }
    }

    /**
     * Maps a list of Markdown heading lines (e.g. {@code "## Installation"}) to their
     * 0-based indices in {@code sections}.  A section "matches" a heading when the
     * first non-blank line of that section equals the heading string (after trimming).
     */
    private List<Integer> resolveHeadingIndices(List<String> headings, List<String> sections) {
        List<Integer> result = new ArrayList<>();
        Set<String> normalized = new java.util.HashSet<>();
        for (String h : headings) normalized.add(h.trim());

        for (int i = 0; i < sections.size(); i++) {
            String firstLine = sections.get(i).lines()
                    .filter(l -> !l.isBlank())
                    .findFirst()
                    .orElse("")
                    .trim();
            if (normalized.contains(firstLine)) {
                result.add(i);
            }
        }
        return result;
    }

    /**
     * Fetches the current (last-merged) translation file from GitHub.
     * Returns an empty string if the file does not exist yet.
     */
    private String fetchExistingTranslation(RepositoryEntity repo, TranslationFileEntity file, String branch) {
        try {
            return gitHubApiService.getFileContent(
                    repo.getFullName(), file.getTargetPath(), repo.getInstallationId(), branch);
        } catch (Exception e) {
            log.debug("Existing translation not found for {} ({}): {}", file.getTargetPath(), branch, e.getMessage());
            return "";
        }
    }

    private synchronized void updateTaskProgress(Long taskId, boolean success, long tokensUsed) {
        taskRepository.findById(taskId).ifPresent(task -> {
            if (success) {
                task.setCompletedFiles(task.getCompletedFiles() + 1);
            } else {
                task.setFailedFiles(task.getFailedFiles() + 1);
            }
            task.setTokensUsed(task.getTokensUsed() + tokensUsed);
            taskRepository.save(task);
        });
    }

    private void completeTask(TranslationTaskEntity task, RepositoryEntity repo,
                              Long userId, Map<String, String> translatedFiles) {
        // Create PR only if there are translated files
        if (!translatedFiles.isEmpty()) {
            try {
                pullRequestService.createOrUpdatePR(task, repo, translatedFiles);
            } catch (Exception e) {
                log.error("Failed to create PR for task {}", task.getId(), e);
                task.setErrorMessage("翻译完成，但 PR 创建失败: " + e.getMessage());
            }
        }

        // Determine final task status based on failed file count
        int failedCount = task.getFailedFiles() != null ? task.getFailedFiles() : 0;
        if (failedCount > 0) {
            task.setStatus("failed");

            // Aggregate error messages from failed files (first 3 at most)
            List<TranslationFileEntity> failedFiles = fileRepository.findByTaskIdAndStatus(task.getId(), "failed");
            String errorSummary = buildErrorSummary(failedCount, failedFiles);
            // Preserve any existing PR error
            if (task.getErrorMessage() != null) {
                task.setErrorMessage(task.getErrorMessage() + " | " + errorSummary);
            } else {
                task.setErrorMessage(errorSummary);
            }
            logService.log(userId, repo.getId(), "translation.failed",
                    "翻译任务 #" + task.getId() + " 有 " + failedCount + " 个文件失败");
        } else {
            task.setStatus("completed");
            logService.log(userId, repo.getId(), "translation.completed",
                    "翻译任务 #" + task.getId() + " 完成，消耗 " + task.getTokensUsed() + " tokens");
        }

        task.setCompletedAt(LocalDateTime.now());

        // Update quota usage
        userRepository.findById(userId).ifPresent(user -> {
            user.setUsedQuota(user.getUsedQuota() + task.getTokensUsed());
            userRepository.save(user);
        });

        // Update repo last sync time
        repo.setLastSyncAt(LocalDateTime.now());
        repoRepository.save(repo);

        taskRepository.save(task);
    }

    private String buildErrorSummary(int failedCount, List<TranslationFileEntity> failedFiles) {
        StringBuilder sb = new StringBuilder();
        sb.append(failedCount).append(" 个文件翻译失败");
        int shown = Math.min(failedFiles.size(), 3);
        for (int i = 0; i < shown; i++) {
            TranslationFileEntity f = failedFiles.get(i);
            sb.append("\n• ").append(f.getSourcePath());
            if (f.getErrorMessage() != null && !f.getErrorMessage().isBlank()) {
                // Extract readable part: strip Java exception prefix if present
                String msg = f.getErrorMessage();
                int colonIdx = msg.indexOf(": ");
                String readable = colonIdx > 0 ? msg.substring(colonIdx + 2) : msg;
                sb.append(": ").append(readable, 0, Math.min(200, readable.length()));
            }
        }
        if (failedFiles.size() > 3) {
            sb.append("\n…以及另外 ").append(failedFiles.size() - 3).append(" 个文件");
        }
        return sb.toString();
    }

    /**
     * For each source file that has at least one completed translation in this task,
     * injects (or replaces) the translation links block at the top of the source file
     * and adds the modified source file to {@code translatedFiles} so it is included
     * in the same PR commit as the translated files.
     * <p>
     * Also merges historical completed translations from the database so that
     * incremental runs always show the full set of available language links.
     */
    private void injectTranslationLinksToSources(RepositoryEntity repo,
                                                 List<TranslationFileEntity> taskFiles,
                                                 Map<String, String> translatedFiles,
                                                 String branchName) {
        // Build sourcePath -> (lang -> targetPath) from this task's completed files
        Map<String, Map<String, String>> sourceToLangPaths = new LinkedHashMap<>();
        for (TranslationFileEntity f : taskFiles) {
            if ("completed".equals(f.getStatus())) {
                sourceToLangPaths
                        .computeIfAbsent(f.getSourcePath(), k -> new LinkedHashMap<>())
                        .put(f.getTargetLanguage(), f.getTargetPath());
            }
        }

        if (sourceToLangPaths.isEmpty()) return;

        // Merge historical completed translations so all language links are shown
        for (String sourcePath : sourceToLangPaths.keySet()) {
            fileRepository.findByRepositoryIdAndSourcePathAndStatus(repo.getId(), sourcePath, "completed")
                    .forEach(h -> sourceToLangPaths.get(sourcePath)
                            .putIfAbsent(h.getTargetLanguage(), h.getTargetPath()));
        }

        // Fetch each source file and inject the links block
        for (Map.Entry<String, Map<String, String>> entry : sourceToLangPaths.entrySet()) {
            String sourcePath = entry.getKey();
            Map<String, String> langPaths = entry.getValue();
            try {
                String sourceContent = gitHubApiService.getFileContent(
                        repo.getFullName(), sourcePath, repo.getInstallationId(), branchName);
                String modified = linkInjector.injectLinks(sourceContent, sourcePath, langPaths);
                if (!modified.equals(sourceContent)) {
                    translatedFiles.put(sourcePath, modified);
                    log.info("Injected translation links into source file: {}", sourcePath);
                }
            } catch (Exception e) {
                log.warn("Failed to inject translation links for {}: {}", sourcePath, e.getMessage());
            }
        }
    }

    private String sha256(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }
}
