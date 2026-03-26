package com.gitdoc.translation.repo.dto;

import lombok.Data;

@Data
public class AvailableRepoDTO {
    private Long githubRepoId;
    private Long installationId;
    private String fullName;
    private String defaultBranch;
    private String description;
    private Integer starCount;
    private String language;
    private Boolean isPrivate;
    private Boolean alreadyAdded;
}
