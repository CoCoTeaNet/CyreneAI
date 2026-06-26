package net.cocotea.cyreneai.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenStreamingChatModel;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO.ChatMessageDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.service.AiConversationService;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

@Slf4j
@Valid
@Controller
@Mapping("/ai/chat")
public class ChatController {

    @Db
    private LightDao lightDao;

    @Inject
    private AiConversationService conversationService;

    @Get @Mapping("/ping")
    public String ping() {
        ChatModel model = buildDefaultModel();
        if (model == null) {
            return "Error: no enabled model provider found in database";
        }
        return model.chat("Ping! ~~~///(^v^)\\\\~~~");
    }

    @Post @Mapping("/stream")
    public void stream(@Body ChatRequestDTO request) throws IOException {
        Context ctx = Context.current();
        ctx.contentType("text/event-stream;charset=utf-8");
        ctx.headerAdd("Cache-Control", "no-cache");
        ctx.headerAdd("Connection", "keep-alive");
        ctx.headerAdd("X-Accel-Buffering", "no");

        OutputStream out = ctx.outputStream();
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder responseContent = new StringBuilder();
        final int[] tokenCounts = new int[3]; // prompt, completion, total

        try {
            AiModel aiModel = getAiModel(request.getModelId());
            if (aiModel == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "No available model")));
                out.flush();
                return;
            }

            StreamingChatModel model = buildStreamingModel(aiModel, request);
            if (model == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "No available model provider")));
                out.flush();
                return;
            }

            List<ChatMessage> messages = convertMessages(request.getMessages(), request.getSystemPrompt());
            messages = truncateMessages(messages, aiModel.getContextWindow());
            model.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    try {
                        responseContent.append(token);
                        writeSseData(out, JSONUtil.toJsonStr(Map.of("content", token)));
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE write error", e);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    try {
                        if (response.metadata() != null && response.metadata().tokenUsage() != null) {
                            var tokenUsage = response.metadata().tokenUsage();
                            tokenCounts[0] = tokenUsage.inputTokenCount();
                            tokenCounts[1] = tokenUsage.outputTokenCount();
                            tokenCounts[2] = tokenCounts[0] + tokenCounts[1];
                            writeSseData(out, JSONUtil.toJsonStr(Map.of(
                                    "type", "token_usage",
                                    "promptTokens", tokenCounts[0],
                                    "completionTokens", tokenCounts[1],
                                    "totalTokens", tokenCounts[2]
                            )));
                        }
                        writeSseData(out, "[DONE]");
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE flush on complete error", e);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        writeSseData(out, JSONUtil.toJsonStr(Map.of("error", error.getMessage())));
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE write error on error", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        } catch (Exception e) {
            log.error("Streaming chat error", e);
            try {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", e.getMessage())));
                out.flush();
            } catch (IOException ex) {
                log.error("SSE write error", ex);
            } finally {
                latch.countDown();
            }
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Save messages to database if conversationId is provided
        if (request.getConversationId() != null) {
            saveMessages(request, responseContent.toString(), tokenCounts);
        }
    }

    private void saveMessages(ChatRequestDTO request, String responseContent, int[] tokenCounts) {
        try {
            // Save the last user message
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                ChatMessageDTO lastUserMsg = request.getMessages().getLast();
                if ("user".equals(lastUserMsg.getRole())) {
                    net.cocotea.cyreneai.model.po.AiMessage userMessage = new net.cocotea.cyreneai.model.po.AiMessage();
                    userMessage.setConversationId(request.getConversationId());
                    userMessage.setRole("user");
                    userMessage.setContent(lastUserMsg.getContent());
                    conversationService.saveMessage(userMessage);
                }
            }

            // Save assistant response
            if (!responseContent.isEmpty()) {
                net.cocotea.cyreneai.model.po.AiMessage assistantMessage = new net.cocotea.cyreneai.model.po.AiMessage();
                assistantMessage.setConversationId(request.getConversationId());
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(responseContent);
                assistantMessage.setPromptTokens(tokenCounts[0]);
                assistantMessage.setCompletionTokens(tokenCounts[1]);
                assistantMessage.setTotalTokens(tokenCounts[2]);
                conversationService.saveMessage(assistantMessage);
            }
        } catch (Exception e) {
            log.error("Failed to save messages", e);
        }
    }

    private void writeSseData(OutputStream out, String data) throws IOException {
        String event = "data: " + data + "\n\n";
        out.write(event.getBytes(StandardCharsets.UTF_8));
    }

    private AiModel getAiModel(BigInteger modelId) {
        if (modelId != null) {
            AiModel model = lightDao.load(new AiModel(modelId));
            if (model == null || model.getIsDeleted() == 1 || model.getEnableStatus() != 1) {
                return null;
            }
            return model;
        } else {
            Map<String, Object> modelParam = MapUtil.newHashMap(2);
            modelParam.put("isDefault", 1);
            modelParam.put("enableStatus", 1);
            List<AiModel> models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
            if (models.isEmpty()) {
                modelParam.remove("isDefault");
                models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
            }
            if (models.isEmpty()) {
                return null;
            }
            return models.getFirst();
        }
    }

    private StreamingChatModel buildStreamingModel(AiModel model, ChatRequestDTO request) {
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        if (provider == null || provider.getIsDeleted() == 1 || provider.getEnableStatus() != 1) {
            return null;
        }
        return buildStreamingChatModel(provider, model, request);
    }

    private StreamingChatModel buildStreamingChatModel(AiModelProvider provider, AiModel model, ChatRequestDTO request) {
        String type = provider.getProviderType();
        String apiKey = provider.getApiKey() != null ? provider.getApiKey() : "";
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();
        Double temperature = request.getTemperature();
        Double topP = request.getTopP();
        Integer maxTokens = request.getMaxTokens();

        log.info("model: {}", model);

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
                        .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com");
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            case "anthropic" -> {
                var builder = AnthropicStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl != null ? baseUrl : "https://api.anthropic.com");
                if (temperature != null) builder.temperature(temperature);
                if (topP != null) builder.topP(topP);
                if (maxTokens != null) builder.maxTokens(maxTokens);
                yield builder.build();
            }
            case "ollama" -> {
                var builder = OllamaStreamingChatModel.builder()
                        .baseUrl(baseUrl != null ? baseUrl : "http://localhost:11434")
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
                var builder = OpenAiStreamingChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl);
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

    private List<ChatMessage> convertMessages(List<ChatMessageDTO> dtos, String systemPrompt) {
        List<ChatMessage> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        if (dtos == null) return messages;
        for (ChatMessageDTO dto : dtos) {
            if (dto.getContent() == null || dto.getContent().isBlank()) continue;
            messages.add(switch (dto.getRole()) {
                case "system" -> new SystemMessage(dto.getContent());
                case "assistant" -> new AiMessage(dto.getContent());
                default -> new UserMessage(dto.getContent());
            });
        }
        return messages;
    }

    private List<ChatMessage> truncateMessages(List<ChatMessage> messages, Integer contextWindow) {
        if (contextWindow == null || contextWindow <= 0) {
            return messages;
        }

        // Simple token estimation: ~4 chars per token
        int estimatedTokens = 0;
        for (ChatMessage msg : messages) {
            String text = getTextFromMessage(msg);
            estimatedTokens += text.length() / 4;
        }

        // If within limit, return as is
        if (estimatedTokens <= contextWindow * 0.8) {
            return messages;
        }

        // Keep system message if present, truncate from oldest messages
        List<ChatMessage> truncated = new ArrayList<>();
        int remainingTokens = (int) (contextWindow * 0.8);

        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                int msgTokens = getTextFromMessage(msg).length() / 4;
                remainingTokens -= msgTokens;
                truncated.add(msg);
            }
        }

        // Add messages from newest to oldest until we hit the limit
        List<ChatMessage> nonSystemMessages = messages.stream()
                .filter(m -> !(m instanceof SystemMessage))
                .toList();

        for (int i = nonSystemMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = nonSystemMessages.get(i);
            int msgTokens = getTextFromMessage(msg).length() / 4;
            if (remainingTokens >= msgTokens) {
                remainingTokens -= msgTokens;
                truncated.add(1, msg); // Insert after system message
            } else {
                break;
            }
        }

        log.info("Truncated messages from {} to {} (estimated tokens: {} -> {})",
                messages.size(), truncated.size(), estimatedTokens, estimatedTokens - remainingTokens);
        return truncated;
    }

    private String getTextFromMessage(ChatMessage msg) {
        if (msg instanceof SystemMessage sm) {
            return sm.text();
        } else if (msg instanceof AiMessage am) {
            return am.text();
        } else if (msg instanceof UserMessage um) {
            return um.singleText();
        }
        return "";
    }

    private ChatModel buildDefaultModel() {
        Map<String, Object> modelParam = MapUtil.newHashMap(2);
        modelParam.put("isDefault", 1);
        modelParam.put("enableStatus", 1);
        List<AiModel> models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
        if (models.isEmpty()) {
            modelParam.remove("isDefault");
            models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
        }
        if (models.isEmpty()) {
            return null;
        }
        AiModel model = models.getFirst();

        Map<String, Object> providerParam = MapUtil.newHashMap(1);
        providerParam.put("id", model.getProviderId());
        List<AiModelProvider> providers = lightDao.find("ai_model_provider_findList", providerParam, AiModelProvider.class);
        if (providers.isEmpty()) {
            return null;
        }
        AiModelProvider provider = providers.getFirst();
        return buildChatModel(provider, model);
    }

    private ChatModel buildChatModel(AiModelProvider provider, AiModel model) {
        String type = provider.getProviderType();
        String apiKey = provider.getApiKey() != null ? provider.getApiKey() : "";
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
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com")
                    .build();
            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.anthropic.com")
                    .build();
            case "ollama" -> OllamaChatModel.builder()
                    .baseUrl(baseUrl != null ? baseUrl : "http://localhost:11434")
                    .modelName(modelName)
                    .build();
            case "gemini" -> GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
            case "custom" -> OpenAiChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .baseUrl(baseUrl)
                    .build();
            default -> {
                log.warn("unsupported provider type: {}", type);
                yield null;
            }
        };
    }
}
