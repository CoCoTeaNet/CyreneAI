package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiRetrievalResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger chunkId;
    private BigInteger documentId;
    private String documentName;
    private String content;
    private Double score;
    private Integer index;
    private String metadata;
}
