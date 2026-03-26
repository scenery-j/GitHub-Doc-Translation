package com.gitdoc.translation.repo.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.common.response.PageResponse;
import com.gitdoc.translation.repo.dto.AddRepoRequest;
import com.gitdoc.translation.repo.dto.AvailableRepoDTO;
import com.gitdoc.translation.repo.dto.RepoDTO;
import com.gitdoc.translation.repo.dto.RepoDetailDTO;
import com.gitdoc.translation.repo.service.RepoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/repos")
@RequiredArgsConstructor
public class RepoController {

    private final RepoService repoService;

    @GetMapping
    public ApiResponse<PageResponse<RepoDTO>> listRepos(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "all") String status) {
        return ApiResponse.ok(repoService.listRepos(userId, page, size, search, status));
    }

    @GetMapping("/available")
    public ApiResponse<PageResponse<AvailableRepoDTO>> listAvailableRepos(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(repoService.listAvailableRepos(userId, page, size));
    }

    @PostMapping
    public ApiResponse<RepoDTO> addRepo(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody AddRepoRequest request) {
        return ApiResponse.ok(repoService.addRepo(userId, request));
    }

    @GetMapping("/{id}")
    public ApiResponse<RepoDetailDTO> getRepo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ApiResponse.ok(repoService.getRepoDetail(userId, id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> removeRepo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        repoService.removeRepo(userId, id);
        return ApiResponse.ok();
    }

    // Tree endpoint is handled by TransConfigController (supports ?branch param)
}
