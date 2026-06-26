package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.map.MapUtil;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiEmbeddingModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiEmbeddingModelVO;
import net.cocotea.cyreneai.service.rag.EmbeddingService;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class EmbeddingServiceImpl implements EmbeddingService {

    @Db
    private LightDao lightDao;

    private final Map<BigInteger, EmbeddingModel> modelCache = new ConcurrentHashMap<>();

    @Override
    public Embedding embed(String text, AiEmbeddingModel model) {
        EmbeddingModel embeddingModel = getOrCreateModel(model);
        if (embeddingModel == null) return null;
        return embeddingModel.embed(text).content();
    }

    @Override
    public List<Embedding> embedBatch(List<String> texts, AiEmbeddingModel model) {
        EmbeddingModel embeddingModel = getOrCreateModel(model);
        if (embeddingModel == null) return List.of();
        List<TextSegment> segments = texts.stream()
                .map(t -> t == null ? null : TextSegment.from(t))
                .toList();
        return embeddingModel.embedAll(segments).content();
    }

    @Override
    public AiEmbeddingModel getDefaultEmbeddingModel() {
        EntityQuery query = EntityQuery.create()
                .where("is_default = 1 and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        List<AiEmbeddingModel> models = lightDao.findEntity(AiEmbeddingModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("enable_status = 1 and is_deleted = 0")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiEmbeddingModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }

    @Override
    public AiEmbeddingModel getEmbeddingModelById(BigInteger id) {
        return lightDao.load(new AiEmbeddingModel(id));
    }

    @Override
    public boolean add(AiEmbeddingModel model) {
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiEmbeddingModel model) {
        Long updated = lightDao.update(model);
        modelCache.remove(model.getId());
        return updated != null && updated > 0;
    }

    @Override
    public boolean delete(BigInteger id) {
        AiEmbeddingModel model = new AiEmbeddingModel(id);
        model.setIsDeleted(1);
        Long updated = lightDao.update(model);
        modelCache.remove(id);
        return updated != null && updated > 0;
    }

    @Override
    public ApiPage<AiEmbeddingModelVO> listByPage(AiEmbeddingModel query, int pageNo, int pageSize) {
        Map<String, Object> params = MapUtil.newHashMap(3);
        params.put("providerType", query.getProviderType());
        params.put("modelName", query.getModelName());
        params.put("enableStatus", query.getEnableStatus());
        Page<AiEmbeddingModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageNo);
        pageParam.setPageSize(pageSize);
        Page<AiEmbeddingModelVO> page = lightDao.findPage(
                pageParam,
                "ai_embedding_model_findList",
                params,
                AiEmbeddingModelVO.class
        );
        return ApiPage.rest(page);
    }

    @Override
    public List<AiEmbeddingModelVO> listEnabled() {
        EntityQuery eq = EntityQuery.create()
                .where("enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiEmbeddingModel.class, eq).stream()
                .map(m -> {
                    AiEmbeddingModelVO vo = new AiEmbeddingModelVO();
                    vo.setId(m.getId());
                    vo.setProviderType(m.getProviderType());
                    vo.setModelName(m.getModelName());
                    vo.setDimension(m.getDimension());
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    private EmbeddingModel getOrCreateModel(AiEmbeddingModel model) {
        if (model == null) return null;
        return modelCache.computeIfAbsent(model.getId(), k -> buildEmbeddingModel(model));
    }

    private EmbeddingModel buildEmbeddingModel(AiEmbeddingModel model) {
        String type = model.getProviderType();
        String apiKey = model.getApiKey();
        String baseUrl = model.getApiBaseUrl();
        String modelName = model.getModelName();

        if (apiKey == null || apiKey.isBlank()) {
            List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                    EntityQuery.create().where("provider_type = ? and enable_status = 1 and is_deleted = 0")
                            .values(type).orderByDesc("sort"));
            if (!providers.isEmpty()) {
                AiModelProvider provider = providers.getFirst();
                if (apiKey == null || apiKey.isBlank()) apiKey = provider.getApiKey();
                if (baseUrl == null || baseUrl.isBlank()) baseUrl = provider.getApiBaseUrl();
            }
        }
        if (apiKey == null) apiKey = "";

        return switch (type.toLowerCase()) {
            case "dashscope" -> QwenEmbeddingModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName != null ? modelName : "text-embedding-v3")
                    .build();
            case "openai" -> OpenAiEmbeddingModel.builder()
                    .apiKey(apiKey)
                    .modelName(modelName != null ? modelName : "text-embedding-3-small")
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com")
                    .build();
            default -> {
                log.warn("Unsupported embedding provider type: {}", type);
                yield null;
            }
        };
    }
}
