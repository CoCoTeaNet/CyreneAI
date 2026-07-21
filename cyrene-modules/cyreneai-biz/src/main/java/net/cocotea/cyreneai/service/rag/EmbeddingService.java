package net.cocotea.cyreneai.service.rag;

import dev.langchain4j.data.embedding.Embedding;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.vo.AiEmbeddingModelVO;
import net.cocotea.cyreneai.model.vo.AiEmbeddingResultVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface EmbeddingService {

    Embedding embed(String text, AiModel model);

    List<Embedding> embedBatch(List<String> texts, AiModel model);

    /**
     * 对文本列表生成嵌入向量。
     *
     * @param modelId 嵌入模型ID，为空时使用默认嵌入模型
     * @param input   待嵌入的文本列表
     * @return 嵌入结果（含模型名、维度、向量列表）
     */
    AiEmbeddingResultVO embedTexts(BigInteger modelId, List<String> input);

    AiModel getDefaultEmbeddingModel();

    AiModel getEmbeddingModelById(BigInteger id);

    boolean add(AiEmbeddingModelAddDTO dto);

    boolean update(AiEmbeddingModelUpdateDTO dto);

    boolean delete(BigInteger id);

    ApiPage<AiEmbeddingModelVO> listByPage(AiEmbeddingModelPageDTO pageDTO);

    List<AiEmbeddingModelVO> listEnabled();
}
