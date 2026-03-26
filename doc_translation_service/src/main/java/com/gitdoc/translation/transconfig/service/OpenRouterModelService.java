package com.gitdoc.translation.transconfig.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.gitdoc.translation.transconfig.dto.AIModelDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OpenRouterModelService {

    private static final String CACHE_KEY = "openrouter:models";
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final RestClient restClient;
    private final StringRedisTemplate redisTemplate;
    private final String apiKey;
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

    public OpenRouterModelService(
            StringRedisTemplate redisTemplate,
            @Value("${spring.ai.openai.api-key:placeholder}") String apiKey) {
        this.redisTemplate = redisTemplate;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    public List<AIModelDTO> getAvailableModels() {
        // 1. Try Redis cache (24h TTL)
        try {
            String cached = redisTemplate.opsForValue().get(CACHE_KEY);
            if (cached != null) {
                List<AIModelDTO> cachedModels = objectMapper.readValue(cached,
                        objectMapper.getTypeFactory().constructCollectionType(List.class, AIModelDTO.class));
                log.debug("Loaded {} models from Redis cache", cachedModels.size());
                return cachedModels;
            }
        } catch (Exception e) {
            log.warn("Failed to read models from Redis cache, re-fetching: {}", e.getMessage());
        }

        // 2. Fetch from OpenRouter API
        List<AIModelDTO> models = fetchFromOpenRouter();

        // 3. Persist to Redis cache
        try {
            redisTemplate.opsForValue().set(CACHE_KEY, objectMapper.writeValueAsString(models), CACHE_TTL);
        } catch (Exception e) {
            log.warn("Failed to cache models in Redis: {}", e.getMessage());
        }
        return models;
    }

    private List<AIModelDTO> fetchFromOpenRouter() {
        try {
            JsonNode response = restClient.get()
                    .uri("/models")
                    .retrieve()
                    .body(JsonNode.class);

            if (response == null || !response.has("data")) {
                return getFallbackModels();
            }

            List<AIModelDTO> models = new ArrayList<>();
            for (JsonNode model : response.get("data")) {
                try {
                    AIModelDTO dto = parseModel(model);
                    if (dto != null) models.add(dto);
                } catch (Exception e) {
                    log.debug("Failed to parse model: {}", e.getMessage());
                }
            }

            models.sort(Comparator.comparingDouble(m -> m.getInputPrice() != null ? m.getInputPrice() : 999.0));
            return models.isEmpty() ? getFallbackModels() : models;
        } catch (Exception e) {
            log.warn("Failed to fetch OpenRouter models, using fallback list: {}", e.getMessage());
            return getFallbackModels();
        }
    }

    private AIModelDTO parseModel(JsonNode model) {
        if (!model.has("id")) return null;

        // Only text->text models
        if (model.has("architecture")) {
            JsonNode arch = model.get("architecture");
            if (arch.has("modality") && !arch.get("modality").asText().contains("text->text")) {
                return null;
            }
        }

        AIModelDTO dto = new AIModelDTO();
        dto.setId(model.get("id").asText());
        dto.setName(model.has("name") ? model.get("name").asText() : dto.getId());
        dto.setContextLength(model.has("context_length") ? model.get("context_length").asInt() : null);

        if (model.has("pricing")) {
            JsonNode pricing = model.get("pricing");
            if (pricing.has("prompt")) {
                // OpenRouter pricing is per token, convert to per 1M
                double promptPrice = pricing.get("prompt").asDouble(0) * 1_000_000;
                dto.setInputPrice(Math.round(promptPrice * 1000.0) / 1000.0);
            }
            if (pricing.has("completion")) {
                double completionPrice = pricing.get("completion").asDouble(0) * 1_000_000;
                dto.setOutputPrice(Math.round(completionPrice * 1000.0) / 1000.0);
            }
        }
        // Free model: both input and output price are 0
        boolean free = (dto.getInputPrice() != null && dto.getInputPrice() == 0.0)
                && (dto.getOutputPrice() != null && dto.getOutputPrice() == 0.0);
        dto.setIsFree(free);
        return dto;
    }

    private List<AIModelDTO> getFallbackModels() {
        List<AIModelDTO> models = new ArrayList<>();

        AIModelDTO deepseek = new AIModelDTO();
        deepseek.setId("deepseek/deepseek-chat");
        deepseek.setName("DeepSeek Chat");
        deepseek.setContextLength(65536);
        deepseek.setInputPrice(0.32);
        deepseek.setOutputPrice(0.89);
        models.add(deepseek);

        AIModelDTO gemini = new AIModelDTO();
        gemini.setId("google/gemini-2.5-flash");
        gemini.setName("Gemini 2.5 Flash");
        gemini.setContextLength(1048576);
        gemini.setInputPrice(0.30);
        gemini.setOutputPrice(2.50);
        models.add(gemini);

        AIModelDTO claude = new AIModelDTO();
        claude.setId("anthropic/claude-haiku-4-5");
        claude.setName("Claude 4.5 Haiku");
        claude.setContextLength(200000);
        claude.setInputPrice(1.0);
        claude.setOutputPrice(5.0);
        models.add(claude);

        return models;
    }

    /**
     * Build dynamic recommendations from the actual available model list.
     * Curated entries are matched first; if unavailable, fill from actual free/paid models.
     */
    public Map<String, Object> getRecommended(List<AIModelDTO> models) {
        Set<String> availableIds = models.stream().map(AIModelDTO::getId).collect(Collectors.toSet());
        Map<String, AIModelDTO> idMap = models.stream()
                .collect(Collectors.toMap(AIModelDTO::getId, m -> m, (a, b) -> a));

        // Curated free models in priority order (based on OpenRouter rankings + quality for translation)
        List<String[]> freeCurated = List.of(
                new String[]{"stepfun/step-3.5-flash:free", "极速", "StepFun 最新免费旗舰，速度极快，OpenRouter 排名 Top3"},
                new String[]{"deepseek/deepseek-r1:free", "推荐", "深度推理模型，中文理解出色，逻辑严谨，完全免费"},
                new String[]{"google/gemini-2.5-flash:free", "谷歌", "Google 顶级模型，速度快，多语言能力强"},
                new String[]{"qwen/qwen-2.5-72b-instruct:free", "中文强", "阿里通义千问 72B，中英互译精准，完全免费"},
                new String[]{"meta-llama/llama-3.3-70b-instruct:free", "开源", "Meta 开源旗舰 70B，多语言翻译效果出众"},
                new String[]{"mistralai/mistral-small-3.1-24b-instruct:free", "极速", "Mistral 轻量旗舰，推理速度快，适合批量文档"}
        );

        // Curated paid models in priority order
        List<String[]> paidCurated = List.of(
                new String[]{"deepseek/deepseek-chat", "性价比", "超低价格，中文翻译一流，OpenRouter 综合性价比最高"},
                new String[]{"deepseek/deepseek-v3.2", "新锐", "DeepSeek 最新版，能力全面提升，性价比依然出色"},
                new String[]{"google/gemini-2.5-flash", "速度快", "超长上下文 1M，谷歌最新，速度与质量兼顾"},
                new String[]{"anthropic/claude-haiku-4-5", "高质量", "Anthropic 旗舰，指令遵循精准，翻译语气自然地道"},
                new String[]{"openai/gpt-4o-mini", "经典", "OpenAI 高性价比，翻译准确，生态成熟经过海量验证"},
                new String[]{"meta-llama/llama-4-maverick", "新锐", "Meta 最新旗舰，文档翻译质量优秀，多语言支持完善"}
        );

        List<Map<String, String>> freeRecs = buildRecs(freeCurated, availableIds, idMap,
                models.stream().filter(m -> Boolean.TRUE.equals(m.getIsFree())).collect(Collectors.toList()), 5);
        List<Map<String, String>> paidRecs = buildRecs(paidCurated, availableIds, idMap,
                models.stream().filter(m -> !Boolean.TRUE.equals(m.getIsFree())).collect(Collectors.toList()), 5);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("free", freeRecs);
        result.put("paid", paidRecs);
        return result;
    }

    private List<Map<String, String>> buildRecs(List<String[]> curated, Set<String> availableIds,
                                                Map<String, AIModelDTO> idMap,
                                                List<AIModelDTO> fallbackPool, int limit) {
        List<Map<String, String>> recs = new ArrayList<>();
        Set<String> added = new java.util.HashSet<>();

        // 1. Fill from curated list (only if actually available)
        for (String[] entry : curated) {
            if (recs.size() >= limit) break;
            String id = entry[0];
            if (availableIds.contains(id) && added.add(id)) {
                AIModelDTO m = idMap.get(id);
                recs.add(Map.of("id", id, "name", m != null ? m.getName() : id,
                        "tag", entry[1], "desc", entry[2]));
            }
        }

        // 2. Fill remaining slots from actual pool
        for (AIModelDTO m : fallbackPool) {
            if (recs.size() >= limit) break;
            if (added.add(m.getId())) {
                String ctx = m.getContextLength() != null ? m.getContextLength() / 1000 + "K 上下文" : "";
                String price = Boolean.TRUE.equals(m.getIsFree()) ? "完全免费" :
                        (m.getInputPrice() != null ? "$" + m.getInputPrice() + "/1M" : "");
                recs.add(Map.of("id", m.getId(), "name", m.getName(),
                        "tag", Boolean.TRUE.equals(m.getIsFree()) ? "免费" : "付费",
                        "desc", (price.isEmpty() ? "" : price + "，") + ctx));
            }
        }
        return recs;
    }
}
