package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * 预算执行状态(实时计算当前周期已用花费)
 */
@Data
@Accessors(chain = true)
public class AiBudgetStatusVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String name;
    private String scopeType;
    private BigInteger scopeId;
    /** 范围对象展示名(模型名/用户名/全局) */
    private String scopeName;
    private String period;
    /** 预算金额 */
    private BigDecimal amount;
    /** 当前周期已用花费 */
    private BigDecimal usedCost;
    /** 已用占比(0-100) */
    private BigDecimal usagePercent;
    private BigDecimal alertThreshold;
    /** 是否已超支 */
    private Boolean exceeded;
    /** 是否触发告警(达到阈值但未超支) */
    private Boolean alerting;
    /** 当前周期起始时间 */
    private LocalDateTime periodStart;
}
