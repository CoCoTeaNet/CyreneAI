package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiPromptEvalPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRateDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRunDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import net.cocotea.cyreneai.model.po.AiPromptEval;
import net.cocotea.cyreneai.model.po.AiPromptTemplate;
import net.cocotea.cyreneai.model.vo.AiPromptEvalVO;
import net.cocotea.cyreneai.service.AiPromptEvalService;
import net.cocotea.cyreneai.util.PromptTemplateRenderer;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Map;

@Slf4j
@Component
public class AiPromptEvalServiceImpl implements AiPromptEvalService {

    @Db
    private LightDao lightDao;

    @Override
    public AiPromptEvalVO run(AiPromptEvalRunDTO dto) {
        // 1. 定位模板 + 渲染 prompt
        String prompt = dto.getPromptContent();
        AiPromptTemplate template = null;
        if (StrUtil.isBlank(prompt) && StrUtil.isNotBlank(dto.getTemplateId())) {
            template = lightDao.load(new AiPromptTemplate(new BigInteger(dto.getTemplateId())));
            if (template != null) {
                prompt = template.getContent();
            }
        }
        if (StrUtil.isBlank(prompt)) {
            throw new IllegalArgumentException("prompt 内容不能为空");
        }
        String rendered = PromptTemplateRenderer.render(prompt, dto.getVariables());

        // 2. 定位模型 + 构建 ChatModel
        AiModel model = lightDao.load(new AiModel(new BigInteger(dto.getModelId())));
        if (model == null || model.getIsDeleted() == 1) {
            throw new IllegalArgumentException("模型不存在");
        }
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        if (provider == null || provider.getIsDeleted() == 1) {
            throw new IllegalArgumentException("模型提供商不存在");
        }
        ChatModel chatModel = buildChatModel(provider, model);
        if (chatModel == null) {
            throw new IllegalArgumentException("暂不支持的模型提供商: " + provider.getProviderType());
        }

        // 3. 调用模型
        long start = System.currentTimeMillis();
        String output;
        try {
            output = chatModel.chat(rendered);
        } catch (Exception e) {
            log.error("prompt eval invoke model failed", e);
            output = "[error] " + e.getMessage();
        }
        long latency = System.currentTimeMillis() - start;

        // 4. 简单估算 token(4字符/token) + 花费
        int promptTokens = rendered.length() / 4;
        int completionTokens = output.length() / 4;
        int totalTokens = promptTokens + completionTokens;
        BigDecimal cost = BigDecimal.ZERO;
        if (model.getInputPrice() != null) {
            cost = cost.add(model.getInputPrice().multiply(BigDecimal.valueOf(promptTokens)).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP));
        }
        if (model.getOutputPrice() != null) {
            cost = cost.add(model.getOutputPrice().multiply(BigDecimal.valueOf(completionTokens)).divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP));
        }

        // 5. 落库
        AiPromptEval eval = new AiPromptEval()
                .setTemplateId(template != null ? template.getId() : null)
                .setTemplateVersion(template != null ? template.getCurrentVersion() : dto.getVersion())
                .setModelId(model.getId())
                .setModelName(model.getModelName())
                .setAbTestId(StrUtil.isNotBlank(dto.getAbTestId()) ? new BigInteger(dto.getAbTestId()) : null)
                .setVariant(dto.getVariant())
                .setInputVariables(dto.getVariables() == null ? null : JSONUtil.toJsonStr(dto.getVariables()))
                .setRenderedPrompt(rendered)
                .setOutput(output)
                .setPromptTokens(promptTokens)
                .setCompletionTokens(completionTokens)
                .setTotalTokens(totalTokens)
                .setCost(cost)
                .setLatencyMs(latency);
        lightDao.save(eval);

        AiPromptEvalVO vo = lightDao.convertType(eval, AiPromptEvalVO.class);
        vo.setTemplateName(template != null ? template.getName() : null);
        return vo;
    }

    @Override
    public boolean rate(AiPromptEvalRateDTO dto) {
        AiPromptEval po = new AiPromptEval()
                .setId(new BigInteger(dto.getId()))
                .setRating(dto.getRating())
                .setFeedback(dto.getFeedback());
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiPromptEvalVO> listByPage(AiPromptEvalPageDTO pageDTO) {
        AiPromptEvalPageDTO.Query query = pageDTO.getAiPromptEval();
        Map<String, Object> map = MapUtil.newHashMap(4);
        map.put("templateId", query != null ? query.getTemplateId() : null);
        map.put("abTestId", query != null ? query.getAbTestId() : null);
        map.put("variant", query != null ? query.getVariant() : null);
        map.put("rating", query != null ? query.getRating() : null);
        Page<AiPromptEvalVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_prompt_eval_findList", map, AiPromptEvalVO.class);
        return ApiPage.rest(page);
    }

    private ChatModel buildChatModel(AiModelProvider provider, AiModel model) {
        String type = provider.getProviderType();
        String apiKey = ApiKeyCipher.decrypt(provider.getApiKey() != null ? provider.getApiKey() : "");
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();
        return switch (type.toLowerCase()) {
            case "dashscope" -> QwenChatModel.builder().apiKey(apiKey).modelName(modelName).build();
            case "openai" -> OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com").build();
            case "anthropic" -> AnthropicChatModel.builder().apiKey(apiKey).modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.anthropic.com").build();
            case "ollama" -> OllamaChatModel.builder()
                    .baseUrl(baseUrl != null ? baseUrl : "http://localhost:11434").modelName(modelName).build();
            case "gemini" -> GoogleAiGeminiChatModel.builder().apiKey(apiKey).modelName(modelName).build();
            case "custom" -> OpenAiChatModel.builder().apiKey(apiKey).modelName(modelName).baseUrl(baseUrl).build();
            default -> null;
        };
    }
}
