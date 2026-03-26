package com.gitdoc.translation.log.service;

import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.entity.OperationLogEntity;
import com.gitdoc.translation.entity.RepositoryEntity;
import com.gitdoc.translation.entity.repository.LogJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    private final LogJpaRepository logRepository;
    private final RepoJpaRepository repoRepository;

    @Async
    public void log(Long userId, Long repositoryId, String action, String detail) {
        try {
            OperationLogEntity logEntity = new OperationLogEntity();
            logEntity.setUserId(userId);
            logEntity.setRepositoryId(repositoryId);
            logEntity.setAction(action);
            logEntity.setDetail(detail);
            logRepository.save(logEntity);
        } catch (Exception e) {
            log.error("Failed to save operation log: action={}", action, e);
        }
    }

    @Async
    public void logByRepoName(String repoFullName, String action, String detail) {
        try {
            repoRepository.findByFullName(repoFullName).ifPresent(repo ->
                    log(repo.getUserId(), repo.getId(), action, detail));
        } catch (Exception e) {
            log.error("Failed to save operation log by repo name", e);
        }
    }

    public PageResponse<OperationLogEntity> getRepoLogs(Long userId, Long repoId, int page, int size) {
        // Validate ownership
        RepositoryEntity repo = repoRepository.findById(repoId).orElseThrow();
        if (!repo.getUserId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        return PageResponse.of(logRepository.findByRepositoryIdOrderByCreatedAtDesc(repoId, pageRequest));
    }
}
