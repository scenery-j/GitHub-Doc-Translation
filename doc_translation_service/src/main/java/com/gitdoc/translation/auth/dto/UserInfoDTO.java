package com.gitdoc.translation.auth.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoDTO {
    private Long id;
    private String username;
    private String avatarUrl;
    private String email;
    private Long freeQuota;
    private Long usedQuota;
    private Boolean hasOpenrouterKey;
    private Boolean hasInstallation;
    private LocalDateTime createdAt;
}
