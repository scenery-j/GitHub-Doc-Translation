package com.gitdoc.translation.translation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gitdoc.translation.common.exception.BusinessException;
import com.gitdoc.translation.common.exception.ErrorCode;
import com.gitdoc.translation.common.util.EncryptionUtil;
import com.gitdoc.translation.entity.UserEntity;
import com.gitdoc.translation.entity.repository.UserJpaRepository;
import com.gitdoc.translation.translation.markdown.MarkdownProcessor;
import com.gitdoc.translation.translation.markdown.ProcessedContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

/**
 * Calls OpenRouter API directly via RestClient (OpenAI-compatible format).
 * This avoids Spring AI version compatibility issues and gives full control.
 */
@Slf4j
@Service
public class TranslationAIService {

    private final MarkdownProcessor markdownProcessor;
    private final UserJpaRepository userRepository;
    private final EncryptionUtil encryptionUtil;
    private final String platformApiKey;
    private final ObjectMapper objectMapper;

    private static final String OPENROUTER_API = "https://openrouter.ai/api/v1/chat/completions";
    private static final int MAX_RETRIES = 3;

    private static final String TRANSLATION_PROMPT_TEMPLATE = """
            你是一位专业的技术文档翻译专家。请将以下 Markdown 文档从 %s 翻译为 %s。
            
            翻译规则（必须严格遵守）：
            1. **保留所有 Markdown 格式**：标题层级(#)、粗体(**)、斜体(*)、列表(- 或 1.)、表格(|)、引用(>)等
            2. **不翻译以下内容**：代码占位符(%%CODEBLOCK_N%% 和 %%INLINE_N%%)、链接 URL（仅翻译链接显示文本）
            3. **技术术语**：API、SDK、Docker、Kubernetes 等通用技术术语保留英文
            4. **保持一致性**：同一术语在整篇文档中使用相同翻译
            5. **自然流畅**：符合 %s 的表达习惯，而非逐字翻译
            6. **保持结构完整**：输出的 Markdown 结构必须与原文完全对应
            
            项目上下文：
            - 项目名称：%s
            - 文件路径：%s
            
            请直接输出翻译后的完整 Markdown 文档，不要添加任何额外说明或代码块包裹。
            
            原文：
            %s
            """;

    public TranslationAIService(
            MarkdownProcessor markdownProcessor,
            UserJpaRepository userRepository,
            EncryptionUtil encryptionUtil,
            ObjectMapper objectMapper,
            @Value("${spring.ai.openai.api-key:placeholder}") String platformApiKey) {
        this.markdownProcessor = markdownProcessor;
        this.userRepository = userRepository;
        this.encryptionUtil = encryptionUtil;
        this.objectMapper = objectMapper;
        this.platformApiKey = platformApiKey;
    }

    public TranslationResult translate(String content, String sourceLang, String targetLang,
                                       String projectName, String filePath, Long userId, String modelId) {
        // Pre-process markdown
        ProcessedContent processed = markdownProcessor.preProcess(content);

        String prompt = TRANSLATION_PROMPT_TEMPLATE.formatted(
                langName(sourceLang), langName(targetLang), langName(targetLang),
                projectName, filePath, processed.getProcessedContent()
        );

        String apiKey = resolveApiKey(userId);
        String translatedRaw = callOpenRouterWithRetry(prompt, apiKey, modelId);

        // Post-process: restore code blocks
        String finalContent = markdownProcessor.postProcess(translatedRaw, processed);

        // Estimate tokens: roughly 4 chars per token
        long estimatedTokens = (content.length() + finalContent.length()) / 4;

        return new TranslationResult(finalContent, estimatedTokens);
    }

    private String callOpenRouterWithRetry(String prompt, String apiKey, String modelId) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return callOpenRouter(prompt, apiKey, modelId);
            } catch (Exception e) {
                lastException = e;
                log.warn("Translation attempt {}/{} failed: {}", attempt, MAX_RETRIES, e.getMessage());
                if (attempt < MAX_RETRIES) {
                    try {
                        Thread.sleep((long) Math.pow(2, attempt) * 1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw BusinessException.of(ErrorCode.AI_API_ERROR, "Translation interrupted");
                    }
                }
            }
        }
        throw BusinessException.of(ErrorCode.AI_API_ERROR,
                "AI翻译失败（已重试 " + MAX_RETRIES + " 次）: " + (lastException != null ? lastException.getMessage() : "unknown"));
    }

    /**
     * 以 api 的方式调用接口
     *
     * @param prompt  用户提示词
     * @param apiKey  api key
     * @param modelId 模型id
     * @return 翻译结果
     */
    private String callOpenRouter(String prompt, String apiKey, String modelId) {
        Map<String, Object> requestBody = Map.of(
                "model", modelId,
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.3,
                "max_tokens", 8192
        );

        JsonNode response = RestClient.builder()
                .build()
                .post()
                .uri(OPENROUTER_API)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header("HTTP-Referer", "https://github-doc-translation.app")
                .header("X-Title", "GitHub Doc Translation")
                .body(requestBody)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || !response.has("choices") || response.get("choices").isEmpty()) {
            throw new RuntimeException("OpenRouter returned empty response");
        }

        return response.get("choices").get(0).get("message").get("content").asText();
    }

    /**
     * 尝试获取用户配置的 OpenRouter API Key
     *
     * @param userId repository ID
     * @return platformApiKey 默认配置的 API Key，或者用户配置的 API Key
     */
    private String resolveApiKey(Long userId) {
        if (userId == null) return platformApiKey;
        try {
            UserEntity user = userRepository.findById(userId).orElse(null);
            if (user != null && user.getOpenrouterApiKey() != null) {
                return encryptionUtil.decrypt(user.getOpenrouterApiKey());
            }
        } catch (Exception e) {
            log.warn("Failed to resolve user API key, using platform key");
        }
        return platformApiKey;
    }

    private String langName(String code) {
        return switch (code) {
            case "zh" -> "中文";
            case "en" -> "English";
            case "ja" -> "日本語";
            case "ko" -> "한국어";
            case "es" -> "Español";
            case "fr" -> "Français";
            case "de" -> "Deutsch";
            case "pt" -> "Português";
            case "ru" -> "Русский";
            case "ar" -> "العربية";
            case "hi" -> "हिन्दी";
            default -> code;
        };
    }

    /**
     * Asks the AI to analyse the git diff patch and decide the translation strategy.
     *
     * <p>The AI responds either with the literal string {@code "full"} or a JSON object:
     * <pre>{"strategy":"incremental","sections":["## Heading A","# Heading B"]}</pre>
     * {@code sections} lists the <em>exact</em> heading lines from the new source file
     * whose sections need re-translation.
     *
     * <p>Falls back to {@link IncrementalStrategy#full()} on any error so we never silently skip
     * a translation due to a transient AI issue.
     *
     * @param patch   unified-diff patch from GitHub Compare API (may be {@code null})
     * @param modelId AI model to use
     * @param userId  user for API-key resolution
     * @return the recommended strategy; never {@code null}
     */
    public IncrementalStrategy analyzeIncrementalStrategy(String patch, String modelId, Long userId) {
        if (patch == null || patch.isBlank()) {
            return IncrementalStrategy.full();
        }

        String prompt = """
                你是一位专业的技术文档翻译助手。以下是一个 Markdown 文档的 git diff（unified diff 格式）：
                
                ```diff
                %s
                ```
                
                请分析这个 diff，判断最佳翻译策略：
                - 如果改动影响整体语义（如文档标题、摘要、大量段落、结构调整），请直接回复：full
                - 如果改动局限于少数独立段落，请回复如下 JSON（不要包含 markdown 代码块标记）：
                  {"strategy":"incremental","sections":["## 受影响段落标题1","## 受影响段落标题2"]}
                  sections 中只列出新文件里受影响段落的【完整标题行】（以 # 开头的行）。
                
                只回复 "full" 或上述 JSON，不要包含任何其他内容。
                """.formatted(patch);

        try {
            String apiKey = resolveApiKey(userId);
            String raw = callOpenRouterWithRetry(prompt, apiKey, modelId);
            if (raw == null) return IncrementalStrategy.full();

            String trimmed = raw.trim();
            if (trimmed.equalsIgnoreCase("full") || trimmed.toLowerCase().startsWith("full")) {
                return IncrementalStrategy.full();
            }

            JsonNode json = objectMapper.readTree(trimmed);
            String strategy = json.path("strategy").asText("full");
            if (!"incremental".equals(strategy)) {
                return IncrementalStrategy.full();
            }

            List<String> sections = new java.util.ArrayList<>();
            json.path("sections").forEach(n -> sections.add(n.asText()));
            if (sections.isEmpty()) {
                return IncrementalStrategy.full();
            }

            log.info("AI recommends incremental translation for {} section(s): {}", sections.size(), sections);
            return IncrementalStrategy.incremental(sections);
        } catch (Exception e) {
            log.warn("AI strategy analysis failed, defaulting to full translation: {}", e.getMessage());
            return IncrementalStrategy.full();
        }
    }

    public record TranslationResult(String content, long tokensUsed) {
    }

    /**
     * Result of the incremental-strategy analysis.
     *
     * @param isFull   {@code true}  → translate the whole document
     * @param sections heading lines of sections to (re-)translate when {@code isFull == false}
     */
    public record IncrementalStrategy(boolean isFull, List<String> sections) {
        public static IncrementalStrategy full() {
            return new IncrementalStrategy(true, List.of());
        }

        public static IncrementalStrategy incremental(List<String> sections) {
            return new IncrementalStrategy(false, List.copyOf(sections));
        }
    }
}
