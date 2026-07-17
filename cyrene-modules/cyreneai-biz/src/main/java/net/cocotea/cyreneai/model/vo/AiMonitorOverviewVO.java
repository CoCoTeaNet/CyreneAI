package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 监控总览卡片
 */
@Data
@Accessors(chain = true)
public class AiMonitorOverviewVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long requestCount;
    private Long totalTokens;
    private BigDecimal totalCost;
    private Double avgLatency;
    private Long successCount;
    /** 成功率(0-100) */
    private BigDecimal successRate;
}
