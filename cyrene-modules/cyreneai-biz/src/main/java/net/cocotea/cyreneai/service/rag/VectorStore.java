package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.po.AiDocumentChunk;

import java.math.BigInteger;
import java.util.List;

public interface VectorStore {

    void addChunk(AiDocumentChunk chunk, float[] embedding);

    void addChunks(List<AiDocumentChunk> chunks, List<float[]> embeddings);

    void removeChunk(BigInteger chunkId);

    void removeByDocumentId(BigInteger documentId);

    void removeByKbId(BigInteger kbId);

    List<AiDocumentChunk> search(BigInteger kbId, float[] queryEmbedding, int topK, double minScore);

    List<AiDocumentChunk> searchAll(float[] queryEmbedding, int topK, double minScore);

    void rebuildIndex(BigInteger kbId);

    void init();
}
