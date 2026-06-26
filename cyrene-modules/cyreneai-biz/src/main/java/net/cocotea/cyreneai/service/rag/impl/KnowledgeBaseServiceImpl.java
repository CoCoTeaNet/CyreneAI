package net.cocotea.cyreneai.service.rag.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.map.MapUtil;
import dev.langchain4j.data.embedding.Embedding;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiDocument;
import net.cocotea.cyreneai.model.po.AiDocumentChunk;
import net.cocotea.cyreneai.model.po.AiEmbeddingModel;
import net.cocotea.cyreneai.model.po.AiKbDocument;
import net.cocotea.cyreneai.model.po.AiKnowledgeBase;
import net.cocotea.cyreneai.model.vo.AiKnowledgeBaseVO;
import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;
import net.cocotea.cyreneai.service.rag.EmbeddingService;
import net.cocotea.cyreneai.service.rag.KnowledgeBaseService;
import net.cocotea.cyreneai.service.rag.VectorStore;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Db
    private LightDao lightDao;

    @Inject
    private EmbeddingService embeddingService;

    @Inject
    private VectorStore vectorStore;

    @Override
    public AiKnowledgeBase add(AiKnowledgeBase kb) {
        BigInteger userId = BigInteger.valueOf(StpUtil.getLoginIdAsLong());
        kb.setCreateBy(userId);
        kb.setCreateTime(LocalDateTime.now());
        if (kb.getIsDeleted() == null) kb.setIsDeleted(0);
        if (kb.getEnableStatus() == null) kb.setEnableStatus(1);
        if (kb.getTopK() == null) kb.setTopK(5);
        if (kb.getChunkSize() == null) kb.setChunkSize(500);
        if (kb.getChunkOverlap() == null) kb.setChunkOverlap(50);
        if (kb.getChunkStrategy() == null) kb.setChunkStrategy("paragraph");
        if (kb.getRetrievalStrategy() == null) kb.setRetrievalStrategy("top_k");
        lightDao.save(kb);
        return kb;
    }

    @Override
    public boolean update(AiKnowledgeBase kb) {
        Long updated = lightDao.update(kb);
        return updated != null && updated > 0;
    }

    @Override
    public boolean delete(BigInteger id) {
        AiKnowledgeBase kb = new AiKnowledgeBase(id);
        kb.setIsDeleted(1);
        Long updated = lightDao.update(kb);
        return updated != null && updated > 0;
    }

    @Override
    public AiKnowledgeBase getById(BigInteger id) {
        return lightDao.load(new AiKnowledgeBase(id));
    }

    @Override
    public ApiPage<AiKnowledgeBaseVO> listByPage(AiKnowledgeBase query, int pageNo, int pageSize) {
        Map<String, Object> params = new HashMap<>();
        params.put("name", query.getName());
        params.put("enableStatus", query.getEnableStatus());
        // find() 对 VO 类映射可靠，手动内存分页
        List<AiKnowledgeBaseVO> all = lightDao.find(
                "ai_knowledge_base_findList", params, AiKnowledgeBaseVO.class
        );
        long total = all.size();
        int from = (pageNo - 1) * pageSize;
        int to = Math.min(from + pageSize, all.size());
        List<AiKnowledgeBaseVO> rows = from < all.size() ? all.subList(from, to) : List.of();
        // 构造 Page 对象以复用 ApiPage.rest()
        Page<AiKnowledgeBaseVO> p = new Page<>(pageNo, pageSize);
        p.setRecordCount(total);
        return ApiPage.rest(p, rows);
    }

    @Override
    public List<AiKnowledgeBaseVO> listEnabled() {
        Map<String, Object> params = MapUtil.newHashMap(1);
        params.put("enableStatus", 1);
        return lightDao.find("ai_knowledge_base_findList", params, AiKnowledgeBaseVO.class);
    }

    @Override
    public List<AiRetrievalResultVO> retrieve(BigInteger kbId, String queryText, int topK,
                                               Double similarityThreshold, String retrievalStrategy) {
        AiKnowledgeBase kb = lightDao.load(new AiKnowledgeBase(kbId));
        if (kb == null) return List.of();

        int k = topK > 0 ? topK : (kb.getTopK() != null ? kb.getTopK() : 5);
        double threshold = similarityThreshold != null ? similarityThreshold :
                (kb.getSimilarityThreshold() != null ? kb.getSimilarityThreshold().doubleValue() : 0.7);

        AiEmbeddingModel embeddingModel;
        if (kb.getEmbeddingModelId() != null) {
            embeddingModel = embeddingService.getEmbeddingModelById(kb.getEmbeddingModelId());
        } else {
            embeddingModel = embeddingService.getDefaultEmbeddingModel();
        }
        if (embeddingModel == null) {
            log.warn("No embedding model available for knowledge base: {}", kbId);
            return List.of();
        }

        Embedding queryEmbedding = embeddingService.embed(queryText, embeddingModel);
        if (queryEmbedding == null) return List.of();

        List<AiDocumentChunk> chunks;
        if ("top_k".equals(retrievalStrategy) || retrievalStrategy == null) {
            chunks = vectorStore.search(kbId, queryEmbedding.vector(), k, threshold);
        } else if ("mmr".equals(retrievalStrategy)) {
            chunks = searchMMR(kbId, queryEmbedding.vector(), k, threshold);
        } else {
            chunks = vectorStore.search(kbId, queryEmbedding.vector(), k, threshold);
        }

        // Fetch document names
        List<BigInteger> docIds = chunks.stream()
                .map(AiDocumentChunk::getDocumentId)
                .distinct()
                .toList();
        Map<BigInteger, String> docNames = docIds.stream()
                .map(id -> lightDao.load(new AiDocument(id)))
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(AiDocument::getId, AiDocument::getName));

        List<AiRetrievalResultVO> results = new ArrayList<>();
        for (AiDocumentChunk chunk : chunks) {
            AiRetrievalResultVO vo = new AiRetrievalResultVO();
            vo.setChunkId(chunk.getId());
            vo.setDocumentId(chunk.getDocumentId());
            vo.setDocumentName(docNames.get(chunk.getDocumentId()));
            vo.setContent(chunk.getContent());
            vo.setIndex(chunk.getChunkIndex());
            vo.setMetadata(chunk.getMetadata());
            results.add(vo);
        }

        log.info("Retrieved {} results from knowledge base {}", results.size(), kbId);
        return results;
    }

    private List<AiDocumentChunk> searchMMR(BigInteger kbId, float[] queryEmbedding, int topK, double minScore) {
        List<AiDocumentChunk> candidates = vectorStore.search(kbId, queryEmbedding, topK * 3, minScore);
        if (candidates.size() <= topK) return candidates;

        List<AiDocumentChunk> selected = new ArrayList<>();
        List<AiDocumentChunk> remaining = new ArrayList<>(candidates);

        double lambda = 0.5;

        while (selected.size() < topK && !remaining.isEmpty()) {
            double bestScore = -Double.MAX_VALUE;
            AiDocumentChunk best = null;

            for (AiDocumentChunk candidate : remaining) {
                float[] candEmb = parseChunkEmbedding(candidate);

                double simToQuery = cosineSimilarity(queryEmbedding, candEmb);
                double maxSimToSelected = 0;
                for (AiDocumentChunk sel : selected) {
                    float[] selEmb = parseChunkEmbedding(sel);
                    double sim = cosineSimilarity(candEmb, selEmb);
                    maxSimToSelected = Math.max(maxSimToSelected, sim);
                }
                double mmrScore = lambda * simToQuery - (1 - lambda) * maxSimToSelected;

                if (mmrScore > bestScore) {
                    bestScore = mmrScore;
                    best = candidate;
                }
            }

            if (best != null) {
                selected.add(best);
                remaining.remove(best);
            }
        }

        return selected;
    }

    private float[] parseChunkEmbedding(AiDocumentChunk chunk) {
        if (chunk.getEmbedding() == null) return new float[0];
        try {
            List<Double> list = cn.hutool.json.JSONUtil.toList(chunk.getEmbedding(), Double.class);
            float[] result = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            return new float[0];
        }
    }

    private double cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length || a.length == 0) return 0;
        double dotProduct = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dotProduct += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }
        double denom = Math.sqrt(normA) * Math.sqrt(normB);
        return denom == 0 ? 0 : dotProduct / denom;
    }

    @Override
    public void addDocument(BigInteger kbId, BigInteger documentId) {
        AiKbDocument relation = new AiKbDocument();
        relation.setKbId(kbId);
        relation.setDocumentId(documentId);
        relation.setCreateTime(LocalDateTime.now());
        try {
            lightDao.save(relation);
        } catch (Exception e) {
            log.warn("Relation already exists: kb={}, doc={}", kbId, documentId);
        }
    }

    @Override
    public void removeDocument(BigInteger kbId, BigInteger documentId) {
        EntityQuery query = EntityQuery.create()
                .where("kb_id = :kbId and document_id = :documentId")
                .names("kbId", "documentId")
                .values(kbId, documentId);
        lightDao.deleteByQuery(AiKbDocument.class, query);
    }

    @Override
    public List<AiKnowledgeBaseVO> getKbsForChat(BigInteger conversationId) {
        return listEnabled();
    }
}
