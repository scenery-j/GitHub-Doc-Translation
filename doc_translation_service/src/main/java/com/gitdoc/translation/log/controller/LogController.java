package com.gitdoc.translation.log.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.entity.OperationLogEntity;
import com.gitdoc.translation.log.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogController {

    private final OperationLogService logService;

    @GetMapping("/api/repos/{id}/logs")
    public ApiResponse<PageResponse<OperationLogEntity>> getRepoLogs(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(logService.getRepoLogs(userId, id, page, size));
    }
}
