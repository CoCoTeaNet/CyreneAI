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
public class AiQuotaAlertVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String scope;

    private BigInteger apiKeyId;

    private String apiKeyName;

    private String metric;

    private Integer thresholdPercent;

    private BigDecimal thresholdValue;

    private String notifyChannel;

    private String notifyTarget;

    private LocalDateTime lastTriggeredTime;

    private Integer triggerCount;

    private Integer enableStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
