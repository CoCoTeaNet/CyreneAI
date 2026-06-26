package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiKnowledgeBaseQueryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger kbId;

    private String query;

    private Integer topK;

    private Double similarityThreshold;

    private String retrievalStrategy;
}
