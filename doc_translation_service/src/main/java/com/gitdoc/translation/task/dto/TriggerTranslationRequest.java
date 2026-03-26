package com.gitdoc.translation.task.dto;

import lombok.Data;

import java.util.List;

@Data
public class TriggerTranslationRequest {
    private String type = "full";       // "full" or "incremental"
    private List<String> branches;      // target branches; empty/null → first configured branch
}
