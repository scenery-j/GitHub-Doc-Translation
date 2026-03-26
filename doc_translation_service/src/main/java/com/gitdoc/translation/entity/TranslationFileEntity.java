package com.gitdoc.translation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "translation_files")
public class TranslationFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "source_path", nullable = false, length = 500)
    private String sourcePath;

    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;

    @Column(name = "target_path", nullable = false, length = 500)
    private String targetPath;

    @Column(nullable = false, length = 20)
    private String status = "pending";

    @Column(name = "source_hash", length = 64)
    private String sourceHash;

    /**
     * Unified-diff patch for this file from the Compare API.
     * Non-null only for webhook-triggered tasks on modified files.
     * Used to ask the AI whether incremental or full translation is needed.
     */
    @Column(name = "patch", columnDefinition = "TEXT")
    private String patch;

    @Column(name = "tokens_used", nullable = false)
    private Long tokensUsed = 0L;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "translated_at")
    private LocalDateTime translatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
