package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.po.AiDocument;
import net.cocotea.cyreneai.model.vo.AiDocumentVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;

public interface DocumentService {

    AiDocument upload(String fileName, byte[] fileContent, BigInteger kbId,
                      String chunkStrategy, Integer chunkSize, Integer chunkOverlap);

    void reIndex(BigInteger documentId);

    void delete(BigInteger id);

    AiDocument getById(BigInteger id);

    ApiPage<AiDocumentVO> listByPage(AiDocument query, int pageNo, int pageSize);

    void processDocument(AiDocument document);
}
