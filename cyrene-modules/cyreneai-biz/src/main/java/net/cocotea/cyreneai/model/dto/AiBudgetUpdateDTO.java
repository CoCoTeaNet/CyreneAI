package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiBudgetUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID不能为空")
    private BigInteger id;

    private String name;

    private String scopeType;

    private BigInteger scopeId;

    private String period;

    private BigDecimal amount;

    private BigDecimal alertThreshold;

    private Integer enableStatus;

    private String remark;
}
