package com.gitdoc.translation.dashboard.dto;

import lombok.Data;

@Data
public class DashboardStatsDTO {
    private Long totalRepos;
    private Long totalTranslatedFiles;
    private Long totalLanguages;
    private Long monthlyTasks;
}
