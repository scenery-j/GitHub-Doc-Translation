package com.gitdoc.translation.repo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddRepoRequest {
    @NotNull(message = "githubRepoId is required")
    private Long githubRepoId;

    @NotBlank(message = "fullName is required")
    private String fullName;

    @NotBlank(message = "defaultBranch is required")
    private String defaultBranch;

    @NotNull(message = "installationId is required")
    private Long installationId;

    private String description;
}
