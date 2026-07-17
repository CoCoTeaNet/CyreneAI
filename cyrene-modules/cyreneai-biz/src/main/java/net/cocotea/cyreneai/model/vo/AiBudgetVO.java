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
public class AiBudgetVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String name;
    private String scopeType;
    private BigInteger scopeId;
    private String period;
    private BigDecimal amount;
    private BigDecimal alertThreshold;
    private Integer enableStatus;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
