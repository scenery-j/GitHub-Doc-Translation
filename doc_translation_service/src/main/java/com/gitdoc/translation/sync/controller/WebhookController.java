package com.gitdoc.translation.sync.controller;

import com.gitdoc.translation.sync.service.WebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WebhookController {

    private final WebhookService webhookService;
    private final StringRedisTemplate redisTemplate;

    @Value("${github.app.webhook-secret}")
    private String webhookSecret;

    /**
     * GitHub webhook handler 订阅的仓库发生事件之后的回调接口
     *
     * @param signature
     * @param event
     * @param deliveryId
     * @param payload
     * @return
     */
    @PostMapping("/api/webhook/github")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "unknown") String event,
            @RequestHeader(value = "X-GitHub-Delivery", defaultValue = "unknown") String deliveryId,
            @RequestBody String payload) {

        // Validate signature
        if (!verifySignature(payload, signature)) {
            log.warn("Invalid webhook signature for delivery: {}", deliveryId);
            return ResponseEntity.status(401).body("Invalid signature");
        }

        // Idempotency check
        String idempotencyKey = "webhook:delivery:" + deliveryId;
        Boolean isNew = redisTemplate.opsForValue().setIfAbsent(idempotencyKey, "1", Duration.ofHours(24));
        if (Boolean.FALSE.equals(isNew)) {
            log.debug("Duplicate webhook delivery: {}", deliveryId);
            return ResponseEntity.ok("OK (duplicate)");
        }

        // Quick respond and process async
        log.info("Received GitHub webhook: event={}, delivery={}", event, deliveryId);

        switch (event) {
            case "push" -> webhookService.processPushEvent(payload);
            case "pull_request" -> webhookService.processPullRequestEvent(payload);
            case "installation" -> webhookService.processInstallationEvent(payload);
            case "installation_repositories" -> webhookService.processInstallationRepositoriesEvent(payload);
            default -> log.debug("Unhandled webhook event type: {}", event);
        }

        return ResponseEntity.ok("OK");
    }

    private boolean verifySignature(String payload, String signature) {
        if (signature == null || !signature.startsWith("sha256=")) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expected = "sha256=" + HexFormat.of().formatHex(hash);
            return expected.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }
}
