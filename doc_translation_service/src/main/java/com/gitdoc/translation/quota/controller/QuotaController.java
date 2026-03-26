package com.gitdoc.translation.quota.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.quota.dto.ApiKeyRequest;
import com.gitdoc.translation.quota.dto.QuotaDTO;
import com.gitdoc.translation.quota.dto.UsageRecordDTO;
import com.gitdoc.translation.quota.service.QuotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class QuotaController {

    private final QuotaService quotaService;

    @GetMapping("/api/quota")
    public ApiResponse<QuotaDTO> getQuota(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(quotaService.getQuota(userId));
    }

    @GetMapping("/api/quota/usage")
    public ApiResponse<PageResponse<UsageRecordDTO>> getUsageHistory(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(quotaService.getUsageHistory(userId, page, size));
    }

    @PutMapping("/api/settings/api-key")
    public ApiResponse<Void> saveApiKey(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ApiKeyRequest request) {
        quotaService.saveApiKey(userId, request);
        return ApiResponse.ok();
    }

    @DeleteMapping("/api/settings/api-key")
    public ApiResponse<Void> deleteApiKey(@AuthenticationPrincipal Long userId) {
        quotaService.deleteApiKey(userId);
        return ApiResponse.ok();
    }

    @PostMapping("/api/settings/api-key/verify")
    public ApiResponse<Map<String, Object>> verifyApiKey(@RequestBody ApiKeyRequest request) {
        boolean valid = quotaService.verifyApiKey(request.getApiKey());
        return ApiResponse.ok(Map.of(
                "valid", valid,
                "message", valid ? "API Key 验证通过" : "API Key 无效，请检查"
        ));
    }
}
