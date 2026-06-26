package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.po.AiKnowledgeBase;
import net.cocotea.cyreneai.model.vo.AiKnowledgeBaseVO;
import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface KnowledgeBaseService {

    AiKnowledgeBase add(AiKnowledgeBase kb);

    boolean update(AiKnowledgeBase kb);

    boolean delete(BigInteger id);

    AiKnowledgeBase getById(BigInteger id);

    ApiPage<AiKnowledgeBaseVO> listByPage(AiKnowledgeBase query, int pageNo, int pageSize);

    List<AiKnowledgeBaseVO> listEnabled();

    List<AiRetrievalResultVO> retrieve(BigInteger kbId, String query, int topK, Double similarityThreshold, String retrievalStrategy);

    void addDocument(BigInteger kbId, BigInteger documentId);

    void removeDocument(BigInteger kbId, BigInteger documentId);

    List<AiKnowledgeBaseVO> getKbsForChat(BigInteger conversationId);
}
