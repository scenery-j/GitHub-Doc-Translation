package com.gitdoc.translation.repo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FileTreeNode {
    private String path;
    private String type; // "file" or "directory"
    private Long size;
    private List<FileTreeNode> children;
}
