package com.gitdoc.translation.dashboard.dto;

import lombok.Data;
import org.stringtemplate.v4.ST;

import java.time.LocalDateTime;

@Data
public class RecentTaskDTO {
    private Long id;
    private Long repositoryId;
    private String repositoryName;
    private String branchName;
    private String triggerType;
    private String status;
    private Integer totalFiles;
    private Integer completedFiles;
    private LocalDateTime createdAt;
    private String prStatus;
}
