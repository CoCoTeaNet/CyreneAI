package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiQuotaAlertAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "告警名称不能为空")
    private String name;

    /** global 全局, key 单Key */
    @NotBlank(message = "作用范围不能为空")
    private String scope;

    private BigInteger apiKeyId;

    /** monthly_tokens, daily_cost, error_rate */
    @NotBlank(message = "监控指标不能为空")
    private String metric;

    private Integer thresholdPercent;

    private BigDecimal thresholdValue;

    private String notifyChannel;

    private String notifyTarget;

    private Integer enableStatus;
}
