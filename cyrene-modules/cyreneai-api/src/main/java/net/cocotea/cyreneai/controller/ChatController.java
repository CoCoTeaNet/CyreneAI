package net.cocotea.cyreneai.controller;

import cn.hutool.core.map.MapUtil;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.model.chat.ChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.validation.annotation.Valid;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.List;
import java.util.Map;

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
