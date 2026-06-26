package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiDocumentChunk;
import net.cocotea.cyreneai.service.rag.VectorStore;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class PgVectorStore implements VectorStore {

    @Db("pgvector")
    private LightDao pgDao;

    @Override
    public void addChunk(AiDocumentChunk chunk, float[] embedding) {
        if (embedding != null) {
            chunk.setEmbedding(vectorToJson(embedding));
        }
        pgDao.save(chunk);
    }

    @Override
    public void addChunks(List<AiDocumentChunk> chunks, List<float[]> embeddings) {
        for (int i = 0; i < chunks.size(); i++) {
            AiDocumentChunk chunk = chunks.get(i);
            if (i < embeddings.size() && embeddings.get(i) != null) {
                chunk.setEmbedding(vectorToJson(embeddings.get(i)));
            }
            pgDao.save(chunk);
        }
    }

    @Override
    public void removeChunk(BigInteger chunkId) {
        pgDao.delete(new AiDocumentChunk(chunkId));
    }

    @Override
    public void removeByDocumentId(BigInteger documentId) {
        EntityQuery query = EntityQuery.create()
                .where("document_id = :documentId")
                .names("documentId").values(documentId);
        pgDao.deleteByQuery(AiDocumentChunk.class, query);
    }

    @Override
    public void removeByKbId(BigInteger kbId) {
        EntityQuery query = EntityQuery.create()
                .where("kb_id = :kbId")
                .names("kbId").values(kbId);
        pgDao.deleteByQuery(AiDocumentChunk.class, query);
    }

    @Override
    public List<AiDocumentChunk> search(BigInteger kbId, float[] queryEmbedding, int topK, double minScore) {
        String jsonEmb = vectorToJson(queryEmbedding);
        double maxDistance = 1.0 - minScore;

        String sql = """
            SELECT id, document_id, kb_id, content, embedding, chunk_index, metadata, create_time
            FROM ai_document_chunk
            WHERE kb_id = :kbId AND embedding IS NOT NULL AND embedding != ''
              AND embedding::vector <=> :queryEmb::vector <= :maxDistance
            ORDER BY embedding::vector <=> :queryEmb::vector
            LIMIT :limit
            """;
        Map<String, Object> params = new HashMap<>();
        params.put("kbId", kbId);
        params.put("queryEmb", jsonEmb);
        params.put("maxDistance", maxDistance);
        params.put("limit", topK);

        return mapRows(pgDao.find(sql, params));
    }

    @Override
    public List<AiDocumentChunk> searchAll(float[] queryEmbedding, int topK, double minScore) {
        String jsonEmb = vectorToJson(queryEmbedding);
        double maxDistance = 1.0 - minScore;

        String sql = """
            SELECT id, document_id, kb_id, content, embedding, chunk_index, metadata, create_time
            FROM ai_document_chunk
            WHERE embedding IS NOT NULL AND embedding != ''
              AND embedding::vector <=> :queryEmb::vector <= :maxDistance
            ORDER BY embedding::vector <=> :queryEmb::vector
            LIMIT :limit
            """;
        Map<String, Object> params = new HashMap<>();
        params.put("queryEmb", jsonEmb);
        params.put("maxDistance", maxDistance);
        params.put("limit", topK);

        return mapRows(pgDao.find(sql, params));
    }

    @Override
    public void rebuildIndex(BigInteger kbId) {
        // pgvector handles indexing natively via SQL; no in-memory index needed
        log.info("PgVectorStore: rebuildIndex is a no-op (native pgvector query)");
    }

    @Override
    public void init() {
        log.info("PgVectorStore initialized, using native pgvector <=> operator");
    }

    private List<AiDocumentChunk> mapRows(List<?> rows) {
        List<AiDocumentChunk> results = new ArrayList<>();
        for (Object r : rows) {
            Map<String, Object> row = (Map<String, Object>) r;
            AiDocumentChunk c = new AiDocumentChunk();
            c.setId(toBigInt(row.get("id")));
            c.setDocumentId(toBigInt(row.get("document_id")));
            c.setKbId(toBigInt(row.get("kb_id")));
            c.setContent((String) row.get("content"));
            c.setEmbedding((String) row.get("embedding"));
            c.setChunkIndex(row.get("chunk_index") != null ? ((Number) row.get("chunk_index")).intValue() : null);
            c.setMetadata((String) row.get("metadata"));
            c.setCreateTime(row.get("create_time") != null
                    ? ((java.sql.Timestamp) row.get("create_time")).toLocalDateTime() : null);
            results.add(c);
        }
        return results;
    }

    private BigInteger toBigInt(Object val) {
        if (val == null) return null;
        if (val instanceof BigInteger bi) return bi;
        if (val instanceof Number n) return BigInteger.valueOf(n.longValue());
        return null;
    }

    private String vectorToJson(float[] vector) {
        List<Double> list = new ArrayList<>(vector.length);
        for (float v : vector) {
            list.add((double) v);
        }
        return JSONUtil.toJsonStr(list);
    }

    public static float[] parseEmbedding(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            List<Double> list = JSONUtil.toList(json, Double.class);
            float[] result = new float[list.size()];
            for (int i = 0; i < list.size(); i++) {
                result[i] = list.get(i).floatValue();
            }
            return result;
        } catch (Exception e) {
            return null;
        }
    }
}
