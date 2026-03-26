package com.gitdoc.translation.dashboard.service;

import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.dashboard.dto.DashboardStatsDTO;
import com.gitdoc.translation.dashboard.dto.RecentTaskDTO;
import com.gitdoc.translation.entity.repository.FileJpaRepository;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final RepoJpaRepository repoRepository;
    private final FileJpaRepository fileRepository;
    private final TaskJpaRepository taskRepository;

    public DashboardStatsDTO getStats(Long userId) {
        List<com.gitdoc.translation.entity.RepositoryEntity> repos = repoRepository.findByUserId(userId,
                PageRequest.of(0, 1000)).getContent();

        long totalRepos = repos.size();
        long totalTranslatedFiles = repos.stream()
                .mapToLong(r -> fileRepository.countByRepositoryIdAndStatus(r.getId(), "completed"))
                .sum();
        long totalLanguages = repos.stream()
                .flatMap(r -> r.getTargetLanguages().stream())
                .distinct().count();

        // Monthly tasks (current month)
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0);
        long monthlyTasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId,
                        PageRequest.of(0, 1000)).stream()
                .filter(t -> t.getCreatedAt().isAfter(monthStart))
                .count();

        DashboardStatsDTO dto = new DashboardStatsDTO();
        dto.setTotalRepos(totalRepos);
        dto.setTotalTranslatedFiles(totalTranslatedFiles);
        dto.setTotalLanguages(totalLanguages);
        dto.setMonthlyTasks(monthlyTasks);
        return dto;
    }

    public PageResponse<RecentTaskDTO> getRecentTasks(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<RecentTaskDTO> result = taskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest)
                .map(task -> {
                    RecentTaskDTO dto = new RecentTaskDTO();
                    dto.setId(task.getId());
                    dto.setRepositoryId(task.getRepositoryId());
                    dto.setBranchName(task.getBranchName());
                    dto.setTriggerType(task.getTriggerType());
                    dto.setStatus(task.getStatus());
                    dto.setTotalFiles(task.getTotalFiles());
                    dto.setCompletedFiles(task.getCompletedFiles());
                    dto.setCreatedAt(task.getCreatedAt());
                    dto.setPrStatus(task.getPrStatus());
                    repoRepository.findById(task.getRepositoryId())
                            .ifPresent(r -> dto.setRepositoryName(r.getFullName()));
                    return dto;
                });
        return PageResponse.of(result);
    }
}
