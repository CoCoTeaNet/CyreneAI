package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Token / 花费 趋势(按日/周/月分组)
 */
@Data
@Accessors(chain = true)
public class AiTokenTrendVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 分组周期标识, 如 2026-07-17 / 2026-28 / 2026-07 */
    private String period;
    private Long promptTokens;
    private Long completionTokens;
    private Long totalTokens;
    private BigDecimal cost;
    private Long requestCount;
}
