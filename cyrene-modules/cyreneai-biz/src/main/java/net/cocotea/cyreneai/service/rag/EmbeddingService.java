package net.cocotea.cyreneai.service.rag;

import dev.langchain4j.data.embedding.Embedding;
import net.cocotea.cyreneai.model.po.AiEmbeddingModel;
import net.cocotea.cyreneai.model.vo.AiEmbeddingModelVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface EmbeddingService {

    Embedding embed(String text, AiEmbeddingModel model);

    List<Embedding> embedBatch(List<String> texts, AiEmbeddingModel model);

    AiEmbeddingModel getDefaultEmbeddingModel();

    AiEmbeddingModel getEmbeddingModelById(BigInteger id);

    boolean add(AiEmbeddingModel model);

    boolean update(AiEmbeddingModel model);

    boolean delete(BigInteger id);

    ApiPage<AiEmbeddingModelVO> listByPage(AiEmbeddingModel model, int pageNo, int pageSize);

    List<AiEmbeddingModelVO> listEnabled();
}
