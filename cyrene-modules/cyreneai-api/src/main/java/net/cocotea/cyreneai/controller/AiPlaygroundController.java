package net.cocotea.cyreneai.controller;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.PlaygroundRunDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiPlaygroundResultVO;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 模型评估 Playground: 单模型测试 / 多模型并排对比
 */
@Slf4j
@Controller
@Mapping("/ai/playground")
@Valid
public class AiPlaygroundController {

    @Db
    private LightDao lightDao;

    @Mapping("/run")
    @Post
    public ApiResult<List<AiPlaygroundResultVO>> run(@Validated @Body PlaygroundRunDTO param) {
        List<AiPlaygroundResultVO> results = new ArrayList<>();
        for (BigInteger modelId : param.getModelIds()) {
            results.add(runOne(modelId, param));
        }
        return ApiResult.ok(results);
    }

    private AiPlaygroundResultVO runOne(BigInteger modelId, PlaygroundRunDTO param) {
        AiPlaygroundResultVO vo = new AiPlaygroundResultVO().setModelId(modelId).setStatus("error");
        long start = System.nanoTime();
        try {
            AiModel model = lightDao.load(new AiModel(modelId));
            if (model == null) {
                return vo.setErrorMsg("模型不存在");
            }
            vo.setModelName(model.getModelName());
            AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
            if (provider == null) {
                return vo.setErrorMsg("模型提供商不存在");
            }
            vo.setProviderType(provider.getProviderType());

            ChatModel chatModel = buildChatModel(provider, model, param);
            if (chatModel == null) {
                return vo.setErrorMsg("不支持的提供商类型: " + provider.getProviderType());
            }

            List<ChatMessage> messages = new ArrayList<>();
            if (param.getSystemPrompt() != null && !param.getSystemPrompt().isEmpty()) {
                messages.add(SystemMessage.from(param.getSystemPrompt()));
            }
            messages.add(UserMessage.from(param.getPrompt()));

            ChatResponse response = chatModel.chat(messages);
            vo.setContent(response.aiMessage() != null ? response.aiMessage().text() : "");
            vo.setLatencyMs((System.nanoTime() - start) / 1_000_000L);

            int promptTokens = 0, completionTokens = 0;
            if (response.metadata() != null && response.metadata().tokenUsage() != null) {
                var usage = response.metadata().tokenUsage();
                promptTokens = usage.inputTokenCount() != null ? usage.inputTokenCount() : 0;
                completionTokens = usage.outputTokenCount() != null ? usage.outputTokenCount() : 0;
            }
            vo.setPromptTokens(promptTokens);
            vo.setCompletionTokens(completionTokens);
            vo.setTotalTokens(promptTokens + completionTokens);
            vo.setCost(calcCost(model, promptTokens, completionTokens));
            vo.setStatus("success");
        } catch (Exception e) {
            log.error("playground run model {} failed", modelId, e);
            vo.setLatencyMs((System.nanoTime() - start) / 1_000_000L);
            vo.setErrorMsg(e.getMessage());
        }
        return vo;
    }

    private BigDecimal calcCost(AiModel model, int promptTokens, int completionTokens) {
        if (model.getInputPrice() == null || model.getOutputPrice() == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal promptCost = BigDecimal.valueOf(promptTokens)
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                .multiply(model.getInputPrice());
        BigDecimal completionCost = BigDecimal.valueOf(completionTokens)
                .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                .multiply(model.getOutputPrice());
        return promptCost.add(completionCost).setScale(6, RoundingMode.HALF_UP);
    }

    private ChatModel buildChatModel(AiModelProvider provider, AiModel model, PlaygroundRunDTO param) {
        String type = provider.getProviderType();
        String apiKey = provider.getApiKey() != null ? provider.getApiKey() : "";
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();
        Double temperature = param.getTemperature();
        Integer maxTokens = param.getMaxTokens();

        return switch (type.toLowerCase()) {
            case "dashscope" -> QwenChatModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName)
                    .build();
            case "openai" -> {
                var b = OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com");
                if (temperature != null) b.temperature(temperature);
                if (maxTokens != null) b.maxTokens(maxTokens);
                yield b.build();
            }
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
            case "custom" -> {
                var b = OpenAiChatModel.builder()
                        .apiKey(apiKey)
                        .modelName(modelName)
                        .baseUrl(baseUrl);
                if (temperature != null) b.temperature(temperature);
                if (maxTokens != null) b.maxTokens(maxTokens);
                yield b.build();
            }
            default -> {
                log.warn("unsupported provider type: {}", type);
                yield null;
            }
        };
    }
}
