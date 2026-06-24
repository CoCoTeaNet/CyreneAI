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
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO.ChatMessageDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
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

        try {
            StreamingChatModel model = buildStreamingModel(request.getModelId());
            if (model == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "No available model")));
                out.flush();
                return;
            }

            List<ChatMessage> messages = convertMessages(request.getMessages());
            model.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    try {
                        writeSseData(out, JSONUtil.toJsonStr(Map.of("content", token)));
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE write error", e);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    try {
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
    }

    private void writeSseData(OutputStream out, String data) throws IOException {
        String event = "data: " + data + "\n\n";
        out.write(event.getBytes(StandardCharsets.UTF_8));
    }

    private StreamingChatModel buildStreamingModel(BigInteger modelId) {
        AiModel model;
        AiModelProvider provider;

        if (modelId != null) {
            model = lightDao.load(new AiModel(modelId));
            if (model == null || model.getIsDeleted() == 1 || model.getEnableStatus() != 1) {
                return null;
            }
            provider = lightDao.load(new AiModelProvider(model.getProviderId()));
            if (provider == null || provider.getIsDeleted() == 1 || provider.getEnableStatus() != 1) {
                return null;
            }
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
            model = models.getFirst();

            Map<String, Object> providerParam = MapUtil.newHashMap(1);
            providerParam.put("id", model.getProviderId());
            List<AiModelProvider> providers = lightDao.find("ai_model_provider_findList", providerParam, AiModelProvider.class);
            if (providers.isEmpty()) {
                return null;
            }
            provider = providers.getFirst();
        }

        return buildStreamingChatModel(provider, model);
    }

    private StreamingChatModel buildStreamingChatModel(AiModelProvider provider, AiModel model) {
        String type = provider.getProviderType();
        String apiKey = provider.getApiKey() != null ? provider.getApiKey() : "";
        String modelName = model.getModelName();

        if ("dashscope".equals(type)) {
            return QwenStreamingChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
        }
        log.warn("unsupported provider type: {}, only dashscope is available", type);
        return null;
    }

    private List<ChatMessage> convertMessages(List<ChatMessageDTO> dtos) {
        List<ChatMessage> messages = new ArrayList<>();
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
        String modelName = model.getModelName();

        if ("dashscope".equals(type)) {
            return QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
        }
        log.warn("unsupported provider type: {}, only dashscope is available", type);
        return null;
    }
}
