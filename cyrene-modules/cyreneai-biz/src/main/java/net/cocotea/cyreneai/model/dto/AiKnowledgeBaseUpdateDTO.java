package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class AiKnowledgeBaseUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String name;
    private String description;
    private BigInteger modelId;
    private BigInteger embeddingModelId;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String chunkStrategy;
    private String retrievalStrategy;
    private Integer topK;
    private BigDecimal similarityThreshold;
    private Integer enableStatus;
    private List<BigInteger> documentIds;
}
