package com.gitdoc.translation.quota.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UsageRecordDTO {
    private Long id;
    private Long taskId;
    private String repositoryName;
    private Long tokensUsed;
    private String source;
    private LocalDateTime createdAt;
}
