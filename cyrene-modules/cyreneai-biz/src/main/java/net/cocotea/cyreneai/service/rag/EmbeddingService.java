package net.cocotea.cyreneai.service.rag;

import dev.langchain4j.data.embedding.Embedding;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.vo.AiEmbeddingModelVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface EmbeddingService {

    Embedding embed(String text, AiModel model);

    List<Embedding> embedBatch(List<String> texts, AiModel model);

    AiModel getDefaultEmbeddingModel();

    AiModel getEmbeddingModelById(BigInteger id);

    boolean add(AiEmbeddingModelAddDTO dto);

    boolean update(AiEmbeddingModelUpdateDTO dto);

    boolean delete(BigInteger id);

    ApiPage<AiEmbeddingModelVO> listByPage(AiEmbeddingModelPageDTO pageDTO);

    List<AiEmbeddingModelVO> listEnabled();
}
