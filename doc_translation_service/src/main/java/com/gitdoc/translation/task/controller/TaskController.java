package com.gitdoc.translation.task.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.task.dto.TaskDTO;
import com.gitdoc.translation.task.dto.TaskDetailDTO;
import com.gitdoc.translation.task.dto.TriggerTranslationRequest;
import com.gitdoc.translation.task.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/repos/{id}/translate")
    public ApiResponse<Map<String, List<Long>>> triggerTranslation(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody(required = false) TriggerTranslationRequest request) {
        if (request == null) {
            request = new TriggerTranslationRequest();
        }
        List<TaskDTO> tasks = taskService.triggerTranslation(userId, id, request);
        List<Long> taskIds = tasks.stream().map(TaskDTO::getId).toList();
        return ApiResponse.ok(Map.of("taskIds", taskIds));
    }

    @GetMapping("/api/repos/{id}/tasks")
    public ApiResponse<PageResponse<TaskDTO>> listTasks(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "all") String status) {
        return ApiResponse.ok(taskService.listTasks(userId, id, page, size, status));
    }

    @GetMapping("/api/tasks/{id}")
    public ApiResponse<TaskDetailDTO> getTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ApiResponse.ok(taskService.getTaskDetail(userId, id));
    }

    @PostMapping("/api/tasks/{id}/cancel")
    public ApiResponse<Void> cancelTask(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        taskService.cancelTask(userId, id);
        return ApiResponse.ok();
    }
}
