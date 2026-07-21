package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;
import net.cocotea.cyreneai.service.rag.RerankService;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.util.ArrayList;
import java.util.List;

/**
 * Rerank 重排序服务实现。
 * <p>
 * 通过 {@code ai_model} 表中 {@code model_type='rerank'} 的默认模型解析重排序供应商，
 * 支持 DashScope（gte-rerank）与 Cohere（rerank）两类 HTTP 接口；
 * 未配置或调用失败时优雅降级，按原顺序截取 top-N。
 */
@Slf4j
@Component
public class RerankServiceImpl implements RerankService {

    @Db
    private LightDao lightDao;

    private static final String DASHSCOPE_URL =
            "https://dashscope.aliyuncs.com/api/v1/services/rerank/text-rerank/text-rerank";
    private static final String COHERE_URL = "https://api.cohere.ai/v1/rerank";

    @Override
    public boolean isAvailable() {
        return getDefaultRerankModel() != null;
    }

    @Override
    public List<AiRetrievalResultVO> rerank(String query, List<AiRetrievalResultVO> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return candidates;
        }
        AiModel model = getDefaultRerankModel();
        if (model == null) {
            return truncate(candidates, topN);
        }
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        if (provider == null || provider.getIsDeleted() == 1 || provider.getEnableStatus() != 1) {
            return truncate(candidates, topN);
        }

        String type = provider.getProviderType() != null ? provider.getProviderType().toLowerCase() : "";
        String apiKey = ApiKeyCipher.decrypt(provider.getApiKey() != null ? provider.getApiKey() : "");
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();

        List<String> documents = candidates.stream().map(AiRetrievalResultVO::getContent).toList();

        try {
            List<int[]> ranked = switch (type) {
                case "dashscope" -> callDashScope(apiKey, baseUrl, modelName, query, documents, topN);
                case "cohere" -> callCohere(apiKey, baseUrl, modelName, query, documents, topN);
                default -> {
                    log.warn("Unsupported rerank provider type: {}", type);
                    yield null;
                }
            };
            if (ranked == null || ranked.isEmpty()) {
                return truncate(candidates, topN);
            }

            List<AiRetrievalResultVO> results = new ArrayList<>();
            for (int[] pair : ranked) {
                int idx = pair[0];
                if (idx < 0 || idx >= candidates.size()) continue;
                AiRetrievalResultVO vo = candidates.get(idx);
                // relevance_score 以千分之一为整数编码传递，还原为 double
                vo.setScore(pair[1] / 1000.0);
                results.add(vo);
                if (results.size() >= topN) break;
            }
            log.info("Rerank returned {} results via {}", results.size(), type);
            return results.isEmpty() ? truncate(candidates, topN) : results;
        } catch (Exception e) {
            log.warn("Rerank failed, fallback to original order: {}", e.getMessage());
            return truncate(candidates, topN);
        }
    }

    /**
     * @return 每个元素为 [原始索引, relevance_score*1000 取整]，按相关性降序
     */
    private List<int[]> callDashScope(String apiKey, String baseUrl, String modelName,
                                      String query, List<String> documents, int topN) {
        String url = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : DASHSCOPE_URL;
        JSONObject body = new JSONObject()
                .set("model", modelName != null ? modelName : "gte-rerank")
                .set("input", new JSONObject()
                        .set("query", query)
                        .set("documents", documents))
                .set("parameters", new JSONObject()
                        .set("return_documents", false)
                        .set("top_n", topN));
        String resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(10000)
                .execute().body();
        JSONObject json = JSONUtil.parseObj(resp);
        JSONObject output = json.getJSONObject("output");
        if (output == null) return null;
        JSONArray resultArr = output.getJSONArray("results");
        return parseResults(resultArr);
    }

    private List<int[]> callCohere(String apiKey, String baseUrl, String modelName,
                                   String query, List<String> documents, int topN) {
        String url = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl : COHERE_URL;
        JSONObject body = new JSONObject()
                .set("model", modelName != null ? modelName : "rerank-multilingual-v3.0")
                .set("query", query)
                .set("documents", documents)
                .set("top_n", topN);
        String resp = HttpRequest.post(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .body(body.toString())
                .timeout(10000)
                .execute().body();
        JSONObject json = JSONUtil.parseObj(resp);
        JSONArray resultArr = json.getJSONArray("results");
        return parseResults(resultArr);
    }

    private List<int[]> parseResults(JSONArray resultArr) {
        if (resultArr == null) return null;
        List<int[]> ranked = new ArrayList<>();
        for (int i = 0; i < resultArr.size(); i++) {
            JSONObject item = resultArr.getJSONObject(i);
            Integer index = item.getInt("index");
            Double score = item.getDouble("relevance_score");
            if (index == null) continue;
            int scoreInt = score != null ? (int) Math.round(score * 1000) : 0;
            ranked.add(new int[]{index, scoreInt});
        }
        return ranked;
    }

    private List<AiRetrievalResultVO> truncate(List<AiRetrievalResultVO> candidates, int topN) {
        if (candidates.size() <= topN) return candidates;
        return new ArrayList<>(candidates.subList(0, topN));
    }

    private AiModel getDefaultRerankModel() {
        EntityQuery query = EntityQuery.create()
                .where("model_type = 'rerank' and is_default = 1 and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        List<AiModel> models = lightDao.findEntity(AiModel.class, query);
        if (models.isEmpty()) {
            query = EntityQuery.create()
                    .where("model_type = 'rerank' and enable_status = 1 and is_deleted = 0")
                    .orderByDesc("sort");
            models = lightDao.findEntity(AiModel.class, query);
        }
        return models.isEmpty() ? null : models.getFirst();
    }
}
