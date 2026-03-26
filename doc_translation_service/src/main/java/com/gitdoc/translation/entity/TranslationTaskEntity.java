package com.gitdoc.translation.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "translation_tasks")
public class TranslationTaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "repository_id", nullable = false)
    private Long repositoryId;

    @Column(name = "trigger_type", nullable = false, length = 20)
    private String triggerType;

    @Column(name = "trigger_commit_sha", length = 64)
    private String triggerCommitSha;

    @Column(name = "branch_name", length = 200)
    private String branchName;

    @Column(nullable = false, length = 20)
    private String status = "queued";

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "target_languages", columnDefinition = "jsonb")
    private List<String> targetLanguages = new ArrayList<>();

    @Column(name = "total_files", nullable = false)
    private Integer totalFiles = 0;

    @Column(name = "completed_files", nullable = false)
    private Integer completedFiles = 0;

    @Column(name = "failed_files", nullable = false)
    private Integer failedFiles = 0;

    @Column(name = "tokens_used", nullable = false)
    private Long tokensUsed = 0L;

    @Column(name = "estimated_cost", precision = 10, scale = 6)
    private BigDecimal estimatedCost = BigDecimal.ZERO;

    @Column(name = "pr_number")
    private Integer prNumber;

    @Column(name = "pr_url", length = 500)
    private String prUrl;

    @Column(name = "pr_branch", length = 200)
    private String prBranch;

    /**
     * PR lifecycle: open / merged / closed. Null before a PR is created.
     */
    @Column(name = "pr_status", length = 20)
    private String prStatus;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
