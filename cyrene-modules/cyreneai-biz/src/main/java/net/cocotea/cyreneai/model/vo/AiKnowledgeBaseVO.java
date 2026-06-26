package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiKnowledgeBaseVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String name;
    private String description;
    private BigInteger modelId;
    private String modelName;
    private BigInteger embeddingModelId;
    private String embeddingModelName;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String chunkStrategy;
    private String retrievalStrategy;
    private Integer topK;
    private BigDecimal similarityThreshold;
    private Integer enableStatus;
    private BigInteger createBy;
    private LocalDateTime createTime;
    private BigInteger updateBy;
    private LocalDateTime updateTime;
    private Integer documentCount;
}
