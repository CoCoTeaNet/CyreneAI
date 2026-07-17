package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.map.MapUtil;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.util.ApiKeyCipher;
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
    public Embedding embed(String text, AiModel model) {
        EmbeddingModel embeddingModel = getOrCreateModel(model);
        if (embeddingModel == null) return null;
        return embeddingModel.embed(text).content();
    }

    @Override
    public List<Embedding> embedBatch(List<String> texts, AiModel model) {
        EmbeddingModel embeddingModel = getOrCreateModel(model);
        if (embeddingModel == null) return List.of();
        List<TextSegment> segments = texts.stream()
                .map(t -> t == null ? null : TextSegment.from(t))
                .toList();
        return embeddingModel.embedAll(segments).content();
    }

    @Override
    public AiModel getDefaultEmbeddingModel() {
        EntityQuery query = EntityQuery.create()
                .where("model_type = 'embedding' and is_default = 1 and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        List<AiModel> models = lightDao.findEntity(AiModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("model_type = 'embedding' and enable_status = 1 and is_deleted = 0")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }

    @Override
    public AiModel getEmbeddingModelById(BigInteger id) {
        return lightDao.load(new AiModel(id));
    }

    @Override
    public boolean add(AiEmbeddingModelAddDTO dto) {
        AiModel model = new AiModel();
        model.setModelType("embedding");
        if (dto.getProviderType() != null) {
            List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                    EntityQuery.create().where("provider_type = ? and is_deleted = 0")
                            .values(dto.getProviderType()).orderByDesc("sort"));
            if (!providers.isEmpty()) {
                model.setProviderId(providers.getFirst().getId());
            }
        }
        model.setModelName(dto.getModelName());
        model.setDimension(dto.getDimension());
        model.setIsDefault(dto.getIsDefault());
        model.setEnableStatus(dto.getEnableStatus());
        model.setSort(dto.getSort());
        model.setRemark(dto.getRemark());
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiEmbeddingModelUpdateDTO dto) {
        AiModel model = new AiModel();
        model.setId(dto.getId());
        model.setModelType("embedding");
        if (dto.getProviderType() != null) {
            List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                    EntityQuery.create().where("provider_type = ? and is_deleted = 0")
                            .values(dto.getProviderType()).orderByDesc("sort"));
            if (!providers.isEmpty()) {
                model.setProviderId(providers.getFirst().getId());
            }
        }
        model.setModelName(dto.getModelName());
        model.setDimension(dto.getDimension());
        model.setIsDefault(dto.getIsDefault());
        model.setEnableStatus(dto.getEnableStatus());
        model.setSort(dto.getSort());
        model.setRemark(dto.getRemark());
        Long updated = lightDao.update(model);
        modelCache.remove(dto.getId());
        return updated != null && updated > 0;
    }

    @Override
    public boolean delete(BigInteger id) {
        AiModel model = new AiModel(id);
        model.setIsDeleted(1);
        Long updated = lightDao.update(model);
        modelCache.remove(id);
        return updated != null && updated > 0;
    }

    @Override
    public ApiPage<AiEmbeddingModelVO> listByPage(AiEmbeddingModelPageDTO pageDTO) {
        AiEmbeddingModelPageDTO.Query q = pageDTO.getEmbeddingModel();
        Map<String, Object> params = MapUtil.newHashMap(3);
        params.put("providerType", q != null ? q.getProviderType() : null);
        params.put("modelName", q != null ? q.getModelName() : null);
        params.put("enableStatus", q != null ? q.getEnableStatus() : null);
        Page<AiEmbeddingModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Page<AiEmbeddingModelVO> page = lightDao.findPage(pageParam, "ai_embedding_model_findList", params, AiEmbeddingModelVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public List<AiEmbeddingModelVO> listEnabled() {
        EntityQuery eq = EntityQuery.create()
                .where("model_type = 'embedding' and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiEmbeddingModelVO vo = new AiEmbeddingModelVO();
                    vo.setId(m.getId());
                    AiModelProvider provider = lightDao.load(new AiModelProvider(m.getProviderId()));
                    vo.setProviderType(provider != null ? provider.getProviderType() : null);
                    vo.setModelName(m.getModelName());
                    vo.setDimension(m.getDimension());
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    return vo;
                }).toList();
    }

    private EmbeddingModel getOrCreateModel(AiModel model) {
        if (model == null) return null;
        return modelCache.computeIfAbsent(model.getId(), k -> buildEmbeddingModel(model));
    }

    private EmbeddingModel buildEmbeddingModel(AiModel model) {
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        String type = provider.getProviderType();
        String apiKey = ApiKeyCipher.decrypt(provider.getApiKey());
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();

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
