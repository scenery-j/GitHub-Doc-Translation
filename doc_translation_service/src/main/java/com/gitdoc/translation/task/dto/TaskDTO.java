package com.gitdoc.translation.task.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDTO {
    private Long id;
    private String repositoryName;
    private String branchName;
    private String triggerType;
    private String status;
    private List<String> targetLanguages;
    private Integer totalFiles;
    private Integer completedFiles;
    private Integer failedFiles;
    private Long tokensUsed;
    private BigDecimal estimatedCost;
    private Integer prNumber;
    private String prUrl;
    private String prStatus;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
}
