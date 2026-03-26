package com.gitdoc.translation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "repositories")
public class RepositoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "github_repo_id", nullable = false)
    private Long githubRepoId;

    @Column(name = "full_name", nullable = false, length = 300)
    private String fullName;

    @Column(length = 500)
    private String description;

    @Column(name = "default_branch", nullable = false, length = 100)
    private String defaultBranch = "main";

    @Column(name = "installation_id", nullable = false)
    private Long installationId;

    @Column(name = "base_language", nullable = false, length = 10)
    private String baseLanguage = "zh";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_languages", columnDefinition = "jsonb")
    private List<String> targetLanguages = new ArrayList<>();

    @Column(name = "ai_model", length = 200)
    private String aiModel;

    @Column(nullable = false, length = 20)
    private String status = "active";

    @Column(name = "last_sync_at")
    private LocalDateTime lastSyncAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
