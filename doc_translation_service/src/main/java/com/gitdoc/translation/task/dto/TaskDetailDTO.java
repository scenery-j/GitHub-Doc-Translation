package com.gitdoc.translation.task.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TaskDetailDTO {
    private Long id;
    private Long repositoryId;
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
    private String errorMessage;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private List<FileProgressDTO> files;

    @Data
    public static class FileProgressDTO {
        private Long id;
        private String sourcePath;
        private String targetLanguage;
        private String targetPath;
        private String status;
        private Long tokensUsed;
        private String errorMessage;
        private LocalDateTime translatedAt;
    }
}
