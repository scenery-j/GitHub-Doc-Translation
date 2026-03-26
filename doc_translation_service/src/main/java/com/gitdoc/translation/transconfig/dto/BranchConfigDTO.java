package com.gitdoc.translation.transconfig.dto;

import lombok.Data;

import java.util.List;

@Data
public class BranchConfigDTO {
    private String branchName;
    private List<String> selectedPaths;
    private List<String> ignorePatterns;
    private String outputPathPattern;
    private Boolean webhookActive;
    private Boolean autoMergePr;
}
