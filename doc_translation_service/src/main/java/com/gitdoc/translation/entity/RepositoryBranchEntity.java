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
@Table(name = "repository_branches",
        uniqueConstraints = @UniqueConstraint(columnNames = {"repository_id", "branch_name"}))
public class RepositoryBranchEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "branch_name", nullable = false, length = 200)
    private String branchName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "selected_paths", columnDefinition = "jsonb")
    private List<String> selectedPaths = new ArrayList<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ignore_patterns", columnDefinition = "jsonb")
    private List<String> ignorePatterns = new ArrayList<>();

    @Column(name = "output_path_pattern", nullable = false, length = 200)
    private String outputPathPattern = "translations/{lang}/";

    @Column(name = "webhook_active", nullable = false)
    private Boolean webhookActive = true;

    @Column(name = "auto_merge_pr", nullable = false)
    private Boolean autoMergePr = false;

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
