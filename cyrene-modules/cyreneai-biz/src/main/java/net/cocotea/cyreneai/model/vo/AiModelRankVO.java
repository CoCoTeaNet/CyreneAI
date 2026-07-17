package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 模型调用排行 / 平均延迟
 */
@Data
@Accessors(chain = true)
public class AiModelRankVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger modelId;
    private String modelName;
    private String providerType;
    private Long requestCount;
    private Long totalTokens;
    private BigDecimal cost;
    private Double avgLatency;
}
