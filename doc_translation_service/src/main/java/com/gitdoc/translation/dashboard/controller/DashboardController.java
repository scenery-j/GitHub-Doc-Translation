package com.gitdoc.translation.dashboard.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.dashboard.dto.DashboardStatsDTO;
import com.gitdoc.translation.dashboard.dto.RecentTaskDTO;
import com.gitdoc.translation.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ApiResponse<DashboardStatsDTO> getStats(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(dashboardService.getStats(userId));
    }

    @GetMapping("/recent-tasks")
    public ApiResponse<PageResponse<RecentTaskDTO>> getRecentTasks(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(dashboardService.getRecentTasks(userId, page, size));
    }
}
