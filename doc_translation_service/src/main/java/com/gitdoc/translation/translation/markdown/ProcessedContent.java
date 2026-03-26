package com.gitdoc.translation.translation.markdown;

import lombok.Data;

import java.util.List;

@Data
public class ProcessedContent {
    private String frontMatter;
    private String processedContent;
    private List<String> placeholders;
}
