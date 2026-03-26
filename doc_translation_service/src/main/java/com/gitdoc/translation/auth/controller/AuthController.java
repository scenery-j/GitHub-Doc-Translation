package com.gitdoc.translation.auth.controller;

import com.gitdoc.translation.auth.dto.UserInfoDTO;
import com.gitdoc.translation.auth.service.AuthService;
import com.gitdoc.translation.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     *  logPage 写死直接掉用接口了， 直接跳转的授权页面
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/github")
    public void redirectToGitHub(HttpServletResponse response) throws IOException {
        String authUrl = authService.buildAuthorizationUrl();
        // 直接跳转到授权页面
        response.sendRedirect(authUrl);
    }

    /**
     * GitHub OAuth callback 授权成功之后的回调，需要获取code和state参数，实现本地登录
     *
     * @param code
     * @param state
     * @param response
     * @throws IOException
     */
    @GetMapping("/github/callback")
    public void callback(@RequestParam String code,
                         @RequestParam String state,
                         HttpServletResponse response) throws IOException {
        String redirectUrl = authService.handleCallback(code, state);
        response.sendRedirect(redirectUrl);
    }

    /**
     * Returns GitHub App installation URL (public endpoint, no auth required)
     */
    @GetMapping("/app-info")
    public ApiResponse<Map<String, String>> getAppInfo() {
        return ApiResponse.ok(authService.getAppInfo());
    }

    @GetMapping("/me")
    public ApiResponse<UserInfoDTO> getCurrentUser(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(authService.getCurrentUser(userId));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.ok();
    }
}
