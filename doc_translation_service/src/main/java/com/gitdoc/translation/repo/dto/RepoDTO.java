package com.gitdoc.translation.repo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RepoDTO {
    private Long id;
    private String fullName;
    private String description;
    private String defaultBranch;
    private String baseLanguage;
    private List<String> targetLanguages;
    private String aiModel;
    private String status;
    private Integer translatedFileCount;
    private LocalDateTime lastSyncAt;
    private LocalDateTime createdAt;
}
