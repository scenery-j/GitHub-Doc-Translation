package com.gitdoc.translation.transconfig.dto;

import lombok.Data;

import java.util.List;

@Data
public class TransConfigDTO {
    private String baseLanguage;
    private List<String> targetLanguages;
    private String aiModel;
}
