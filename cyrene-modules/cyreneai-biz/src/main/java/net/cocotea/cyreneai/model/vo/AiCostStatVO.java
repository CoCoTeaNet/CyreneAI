package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 成本统计(按模型/用户/时间维度)通用结果
 */
@Data
@Accessors(chain = true)
public class AiCostStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 维度键, 如 modelId / userId / period */
    private String dimKey;
    /** 维度展示名 */
    private String dimName;
    private BigDecimal cost;
    private Long totalTokens;
    private Long requestCount;
}
