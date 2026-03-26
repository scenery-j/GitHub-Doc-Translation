package com.gitdoc.translation.transconfig.controller;

import com.gitdoc.translation.common.response.ApiResponse;
import com.gitdoc.translation.repo.dto.FileTreeNode;
import com.gitdoc.translation.repo.service.RepoService;
import com.gitdoc.translation.transconfig.dto.AIModelDTO;
import com.gitdoc.translation.transconfig.dto.BranchConfigDTO;
import com.gitdoc.translation.transconfig.dto.TransConfigDTO;
import com.gitdoc.translation.transconfig.service.OpenRouterModelService;
import com.gitdoc.translation.transconfig.service.TransConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TransConfigController {

    private final TransConfigService transConfigService;
    private final OpenRouterModelService modelService;
    private final RepoService repoService;

    @Autowired
    ChatModel openAiCchatModel;


    @GetMapping("/api/repos/test")
    public String test() {
        String text = ChatClient.builder(openAiCchatModel)
                .defaultSystem("你是专业的翻译家")
                .build()
                .prompt()
                .user("你是谁？")
                .call()
                .chatResponse()
                .getResult().getOutput().getText();
        return text;
    }

    // ── Global config ────────────────────────────────────────────────────────

    @GetMapping("/api/repos/{id}/config")
    public ApiResponse<TransConfigDTO> getConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ApiResponse.ok(transConfigService.getConfig(userId, id));
    }

    @PutMapping("/api/repos/{id}/config")
    public ApiResponse<TransConfigDTO> updateConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody TransConfigDTO request) {
        return ApiResponse.ok(transConfigService.updateConfig(userId, id, request));
    }

    // ── Branch CRUD ──────────────────────────────────────────────────────────

    @GetMapping("/api/repos/{id}/branches")
    public ApiResponse<List<BranchConfigDTO>> listBranches(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ApiResponse.ok(transConfigService.listBranches(userId, id));
    }

    @GetMapping("/api/repos/{id}/branches/available")
    public ApiResponse<List<String>> getAvailableBranches(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id) {
        return ApiResponse.ok(repoService.getAvailableBranches(userId, id));
    }

    @PostMapping("/api/repos/{id}/branches")
    public ApiResponse<BranchConfigDTO> addBranch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ApiResponse.ok(transConfigService.addBranch(userId, id, body.get("branchName")));
    }

    @GetMapping("/api/repos/{id}/branches/config")
    public ApiResponse<BranchConfigDTO> getBranchConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String branch) {
        return ApiResponse.ok(transConfigService.getBranchConfig(userId, id, branch));
    }

    @PutMapping("/api/repos/{id}/branches/config")
    public ApiResponse<BranchConfigDTO> updateBranchConfig(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String branch,
            @RequestBody BranchConfigDTO request) {
        return ApiResponse.ok(transConfigService.updateBranchConfig(userId, id, branch, request));
    }

    @DeleteMapping("/api/repos/{id}/branches")
    public ApiResponse<Void> deleteBranch(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String branch) {
        transConfigService.deleteBranch(userId, id, branch);
        return ApiResponse.ok();
    }

    // ── Ignore content (per-branch) ──────────────────────────────────────────

    @GetMapping("/api/repos/{id}/ignore")
    public ApiResponse<String> getIgnore(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String branch) {
        return ApiResponse.ok(transConfigService.getIgnoreContent(userId, id, branch));
    }

    @PutMapping("/api/repos/{id}/ignore")
    public ApiResponse<Void> updateIgnore(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam String branch,
            @RequestBody Map<String, String> body) {
        transConfigService.updateIgnoreContent(userId, id, branch, body.getOrDefault("content", ""));
        return ApiResponse.ok();
    }

    // ── File tree (branch-aware) ─────────────────────────────────────────────

    @GetMapping("/api/repos/{id}/tree")
    public ApiResponse<List<FileTreeNode>> getFileTree(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestParam(required = false) String branch) {
        return ApiResponse.ok(repoService.getFileTree(userId, id, branch));
    }

    // ── AI Models ────────────────────────────────────────────────────────────

    @GetMapping("/api/models")
    public ApiResponse<Map<String, Object>> getModels() {
        List<AIModelDTO> models = modelService.getAvailableModels();
        Map<String, Object> recommended = modelService.getRecommended(models);
        return ApiResponse.ok(Map.of("models", models, "recommended", recommended));
    }
}
