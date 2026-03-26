package com.gitdoc.translation.quota.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.common.util.EncryptionUtil;
import com.gitdoc.translation.entity.TranslationTaskEntity;
import com.gitdoc.translation.entity.UserEntity;
import com.gitdoc.translation.entity.repository.RepoJpaRepository;
import com.gitdoc.translation.entity.repository.TaskJpaRepository;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import com.gitdoc.translation.quota.dto.ApiKeyRequest;
import com.gitdoc.translation.quota.dto.QuotaDTO;
import com.gitdoc.translation.quota.dto.UsageRecordDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuotaService {

    private final UserJpaRepository userRepository;
    private final TaskJpaRepository taskRepository;
    private final RepoJpaRepository repoRepository;
    private final EncryptionUtil encryptionUtil;

    public QuotaDTO getQuota(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        QuotaDTO dto = new QuotaDTO();
        dto.setFreeQuota(user.getFreeQuota());
        dto.setUsedQuota(user.getUsedQuota());
        dto.setRemaining(Math.max(0, user.getFreeQuota() - user.getUsedQuota()));
        dto.setHasCustomApiKey(user.getOpenrouterApiKey() != null);
        return dto;
    }

    public PageResponse<UsageRecordDTO> getUsageHistory(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page - 1, size);
        Page<TranslationTaskEntity> tasks = taskRepository.findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        return PageResponse.of(tasks.map(task -> {
            UsageRecordDTO dto = new UsageRecordDTO();
            dto.setId(task.getId());
            dto.setTaskId(task.getId());
            dto.setTokensUsed(task.getTokensUsed());
            dto.setCreatedAt(task.getCreatedAt());

            repoRepository.findById(task.getRepositoryId()).ifPresent(repo -> {
                dto.setRepositoryName(repo.getFullName());
                UserEntity user = userRepository.findById(userId).orElse(null);
                dto.setSource(user != null && user.getOpenrouterApiKey() != null ? "custom_key" : "platform");
            });
            return dto;
        }));
    }

    @Transactional
    public void saveApiKey(Long userId, ApiKeyRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        user.setOpenrouterApiKey(encryptionUtil.encrypt(request.getApiKey()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteApiKey(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND));
        user.setOpenrouterApiKey(null);
        userRepository.save(user);
    }

    public boolean verifyApiKey(String apiKey) {
        try {
            RestClient.create().get()
                    .uri("https://openrouter.ai/api/v1/key")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception e) {
            log.info("API key verification failed: {}", e.getMessage());
            return false;
        }
    }
}
