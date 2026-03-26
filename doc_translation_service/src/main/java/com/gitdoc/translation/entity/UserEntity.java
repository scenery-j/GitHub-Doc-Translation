package com.gitdoc.translation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "github_id", nullable = false, unique = true)
    private Long githubId;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(length = 200)
    private String email;

    @Column(name = "github_access_token", columnDefinition = "TEXT")
    private String githubAccessToken;

    @Column(name = "openrouter_api_key", columnDefinition = "TEXT")
    private String openrouterApiKey;

    @Column(name = "free_quota", nullable = false)
    private Long freeQuota = 100000L;

    @Column(name = "used_quota", nullable = false)
    private Long usedQuota = 0L;

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
