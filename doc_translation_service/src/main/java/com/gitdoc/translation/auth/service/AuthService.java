package com.gitdoc.translation.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdoc.translation.auth.dto.UserInfoDTO;
import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.common.util.EncryptionUtil;
import com.gitdoc.translation.common.util.JwtUtil;
import com.gitdoc.translation.entity.UserEntity;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserJpaRepository userRepository;
    private final JwtUtil jwtUtil;
    private final EncryptionUtil encryptionUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${github.app.client-id}")
    private String clientId;

    @Value("${github.app.client-secret}")
    private String clientSecret;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${github.app.slug}")
    private String appSlug;

    private static final String STATE_PREFIX = "oauth:state:";
    private static final Duration STATE_TTL = Duration.ofMinutes(5);

    private final RestClient restClient = RestClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .build();

    /**
     * Generate GitHub OAuth authorize URL with state
     */
    public String buildAuthorizationUrl() {
        String state = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(STATE_PREFIX + state, "1", STATE_TTL);

        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientId +
                "&scope=user:email" +
                "&state=" + state;
    }

    /**
     * Handle OAuth callback: exchange code for token, get user info, issue JWT
     */
    public String handleCallback(String code, String state) {
        // Validate state
        String key = STATE_PREFIX + state;
        Boolean valid = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(valid)) {
            throw BusinessException.of(ErrorCode.PARAM_ERROR, "Invalid or expired OAuth state");
        }
        redisTemplate.delete(key);

        // Exchange code for access token
        String userAccessToken = exchangeCodeForToken(code);

        // Get user info from GitHub
        JsonNode userInfo = getGitHubUserInfo(userAccessToken);
        Long githubId = userInfo.get("id").asLong();
        String username = userInfo.get("login").asText();
        String avatarUrl = userInfo.get("avatar_url").asText();
        String email = userInfo.has("email") && !userInfo.get("email").isNull()
                ? userInfo.get("email").asText() : null;

        // Create or update user
        UserEntity user = userRepository.findByGithubId(githubId).orElse(new UserEntity());
        user.setGithubId(githubId);
        user.setUsername(username);
        user.setAvatarUrl(avatarUrl);
        user.setEmail(email);
        user.setGithubAccessToken(encryptionUtil.encrypt(userAccessToken));
        user = userRepository.save(user);

        // Generate platform JWT
        String token = jwtUtil.generateToken(user.getId());

        // Check if user has GitHub App installation
        boolean hasInstallation = checkInstallation(userAccessToken);

        String redirectPath = hasInstallation ? "/dashboard" : "/setup";
        return frontendUrl + redirectPath + "?token=" + token;
    }

    public UserInfoDTO getCurrentUser(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "User not found"));

        String encryptedToken = user.getGithubAccessToken();
        boolean hasInstallation = false;
        if (encryptedToken != null) {
            try {
                String token = encryptionUtil.decrypt(encryptedToken);
                hasInstallation = checkInstallation(token);
            } catch (Exception e) {
                log.warn("Failed to check installation status for user {}", userId);
            }
        }

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setEmail(user.getEmail());
        dto.setFreeQuota(user.getFreeQuota());
        dto.setUsedQuota(user.getUsedQuota());
        dto.setHasOpenrouterKey(user.getOpenrouterApiKey() != null);
        dto.setHasInstallation(hasInstallation);
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private String exchangeCodeForToken(String code) {
        try {
            Map<String, String> body = Map.of(
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", code
            );

            JsonNode response = restClient.post()
                    .uri("https://github.com/login/oauth/access_token")
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("access_token")) {
                throw BusinessException.of(ErrorCode.GITHUB_API_ERROR, "Failed to get access token from GitHub");
            }
            return response.get("access_token").asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to exchange code for token", e);
            throw BusinessException.of(ErrorCode.GITHUB_API_ERROR, "GitHub OAuth failed: " + e.getMessage());
        }
    }

    private JsonNode getGitHubUserInfo(String accessToken) {
        try {
            return restClient.get()
                    .uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (Exception e) {
            log.error("Failed to get GitHub user info", e);
            throw BusinessException.of(ErrorCode.GITHUB_API_ERROR, "Failed to get user info from GitHub");
        }
    }

    /**
     * Returns GitHub App slug and installation URL for the frontend
     */
    public Map<String, String> getAppInfo() {
        Map<String, String> info = new LinkedHashMap<>();
        info.put("appSlug", appSlug);
        info.put("installUrl", "https://github.com/apps/" + appSlug + "/installations/new");
        return info;
    }

    private boolean checkInstallation(String userAccessToken) {
        try {
            JsonNode response = restClient.get()
                    .uri("https://api.github.com/user/installations")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + userAccessToken)
                    .retrieve()
                    .body(JsonNode.class);
            return response != null && response.has("total_count")
                    && response.get("total_count").asInt() > 0;
        } catch (Exception e) {
            log.warn("Failed to check installations: {}", e.getMessage());
            return false;
        }
    }
}
