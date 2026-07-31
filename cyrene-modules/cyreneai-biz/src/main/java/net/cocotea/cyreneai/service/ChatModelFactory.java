package net.cocotea.cyreneai.service;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import org.noear.solon.annotation.Component;

/**
 * ChatModel 统一构建工厂。
 * <p>
 * 集中各提供商分支、默认 BaseUrl 与 API 密钥解密逻辑，
 * 供流式（SSE）与阻塞（同步）两类模型构建复用，避免控制器内大段重复代码；
 * {@code custom} 类型强制要求配置 {@code apiBaseUrl}，缺失时返回 null 并告警。
 *
 * @author cyrene
 */
@Slf4j
@Component
public class ChatModelFactory {

    private static final String DEFAULT_OPENAI_URL = "https://api.openai.com";
    private static final String DEFAULT_ANTHROPIC_URL = "https://api.anthropic.com";
    private static final String DEFAULT_OLLAMA_URL = "http://localhost:11434";

    /**
     * 构建流式模型；不支持的提供商类型或 custom 缺失 baseUrl 时返回 null
     */
    public StreamingChatModel streaming(AiModelProvider provider, AiModel model,
                                        Double temperature, Double topP, Integer maxTokens) {
        String type = provider.getProviderType();
        String apiKey = apiKeyOf(provider);
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();

        return switch (type.toLowerCase()) {
            case "dashscope" -> {
                var builder = QwenStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName);
                if (temperature != null) builder.temperature(temperature.floatValue());
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            case "openai" -> {
                var builder = OpenAiStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl != null ? baseUrl : DEFAULT_OPENAI_URL);
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            case "anthropic" -> {
                var builder = AnthropicStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl != null ? baseUrl : DEFAULT_ANTHROPIC_URL);
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            case "ollama" -> {
                var builder = OllamaStreamingChatModel.builder()
                        .baseUrl(baseUrl != null ? baseUrl : DEFAULT_OLLAMA_URL)
                        .modelName(modelName);
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.numPredict(maxTokens);
                yield builder.build();
            }
            case "gemini" -> {
                var builder = GoogleAiGeminiStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName);
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxOutputTokens(maxTokens);
                yield builder.build();
            }
            case "custom" -> {
                String customUrl = requireCustomBaseUrl(provider);
                if (customUrl == null) yield null;
                var builder = OpenAiStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(customUrl);
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            default -> {
                log.warn("unsupported provider type: {}", type);
                yield null;
            }
        };
    }

    /**
     * 构建阻塞（同步）模型；不支持的提供商类型或 custom 缺失 baseUrl 时返回 null
     */
    public ChatModel blocking(AiModelProvider provider, AiModel model) {
        String type = provider.getProviderType();
        String apiKey = apiKeyOf(provider);
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();

        return switch (type.toLowerCase()) {
            case "dashscope" -> QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
            case "openai" -> OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : DEFAULT_OPENAI_URL)
                    .build();
            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : DEFAULT_ANTHROPIC_URL)
                    .build();
            case "ollama" -> OllamaChatModel.builder()
                    .baseUrl(baseUrl != null ? baseUrl : DEFAULT_OLLAMA_URL)
                    .modelName(modelName)
                    .build();
            case "gemini" -> GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
            case "custom" -> {
                String customUrl = requireCustomBaseUrl(provider);
                if (customUrl == null) yield null;
                yield OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(customUrl)
                        .build();
            }
            default -> {
                log.warn("unsupported provider type: {}", type);
                yield null;
            }
        };
    }

    private String apiKeyOf(AiModelProvider provider) {
        return ApiKeyCipher.decrypt(provider.getApiKey() != null ? provider.getApiKey() : "");
    }

    /**
     * custom 类型必须显式配置 apiBaseUrl，缺失时返回 null 并告警
     */
    private String requireCustomBaseUrl(AiModelProvider provider) {
        String baseUrl = provider.getApiBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            log.warn("custom provider [{}] missing apiBaseUrl, cannot build model", provider.getId());
            return null;
        }
        return baseUrl;
    }
}
