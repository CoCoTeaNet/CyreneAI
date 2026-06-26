package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class AiKnowledgeBaseAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private String description;
    private BigInteger modelId;
    private BigInteger embeddingModelId;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String chunkStrategy;
    private String retrievalStrategy;
    private Integer topK;
    private Double similarityThreshold;
    private Integer enableStatus;
    private List<BigInteger> documentIds;
}
