package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Playground / 模型对比 单个模型的运行结果
 */
@Data
@Accessors(chain = true)
public class AiPlaygroundResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger modelId;
    private String modelName;
    private String providerType;
    /** 模型输出内容 */
    private String content;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private BigDecimal cost;
    private Long latencyMs;
    /** success / error */
    private String status;
    private String errorMsg;
}
