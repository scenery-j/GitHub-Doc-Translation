package com.gitdoc.translation.repo.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RepoDetailDTO {
    private Long id;
    private String fullName;
    private String defaultBranch;
    private String baseLanguage;
    private List<String> targetLanguages;
    private String aiModel;
    private String status;
    private LocalDateTime lastSyncAt;
    private RepoStats stats;

    @Data
    public static class RepoStats {
        private long totalFiles;
        private long translatedFiles;
        private long pendingChanges;
        private long totalPRs;
        private List<LanguageProgress> languageProgress;
    }

    @Data
    public static class LanguageProgress {
        private String language;
        private long total;
        private long completed;
        private int percentage;
    }
}
