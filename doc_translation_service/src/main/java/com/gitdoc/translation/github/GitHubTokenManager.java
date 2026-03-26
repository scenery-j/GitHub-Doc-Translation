package com.gitdoc.translation.github;

import com.fasterxml.jackson.databind.JsonNode;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.io.StringReader;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.util.Date;

@Slf4j
@Component
public class GitHubTokenManager {

    private static final String CACHE_PREFIX = "github:installation_token:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(50);

    private final StringRedisTemplate redisTemplate;
    private final RSAPrivateKey privateKey;
    private final String appId;
    private final RestClient restClient;

    public GitHubTokenManager(
            StringRedisTemplate redisTemplate,
            @Value("${github.app.id}") String appId,
            @Value("${github.app.private-key}") String privateKeyContent) throws Exception {
        this.redisTemplate = redisTemplate;
        this.appId = appId;
        this.privateKey = loadPrivateKey(privateKeyContent);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.github.com")
                .build();
    }

    public String getInstallationToken(Long installationId) {
        String cacheKey = CACHE_PREFIX + installationId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        String token = generateInstallationToken(installationId);
        redisTemplate.opsForValue().set(cacheKey, token, CACHE_TTL);
        return token;
    }

    /**
     * Evict cached token for an installation (call when installation is known to be deleted/changed).
     */
    public void evictInstallationToken(Long installationId) {
        redisTemplate.delete(CACHE_PREFIX + installationId);
    }

    private String generateInstallationToken(Long installationId) {
        String jwt = generateAppJwt();
        try {
            JsonNode response = restClient.post()
                    .uri("/app/installations/{id}/access_tokens", installationId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("token")) {
                throw new RuntimeException("Failed to get installation token from GitHub");
            }
            return response.get("token").asText();
        } catch (HttpClientErrorException.NotFound e) {
            // installation_id is no longer valid (App was uninstalled/reinstalled)
            log.warn("Installation {} not found on GitHub - App may have been reinstalled", installationId);
            throw new StaleInstallationException(installationId);
        } catch (StaleInstallationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate installation token for installationId={}", installationId, e);
            throw new RuntimeException("GitHub installation token generation failed", e);
        }
    }

    private String generateAppJwt() {
        long nowMs = System.currentTimeMillis();
        return Jwts.builder()
                .issuer(appId)
                .issuedAt(new Date(nowMs - 60_000)) // 60 sec in the past to handle clock skew
                .expiration(new Date(nowMs + 9 * 60 * 1000))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    private RSAPrivateKey loadPrivateKey(String privateKeyContent) throws Exception {
        // 支持环境变量中以 \n 字面量表示换行的情况
        String normalizedPem = privateKeyContent.replace("\\n", "\n");
        try (PEMParser parser = new PEMParser(new StringReader(normalizedPem))) {
            Object obj = parser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            if (obj instanceof PEMKeyPair keyPair) {
                KeyPair kp = converter.getKeyPair(keyPair);
                return (RSAPrivateKey) kp.getPrivate();
            }
            throw new RuntimeException("Unsupported PEM key format: " + obj.getClass().getName());
        }
    }
}
