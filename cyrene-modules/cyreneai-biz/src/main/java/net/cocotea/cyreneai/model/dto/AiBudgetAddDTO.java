package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@Accessors(chain = true)
public class AiBudgetAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "预算名称不能为空")
    private String name;

    /** global, model, user */
    @NotBlank(message = "范围类型不能为空")
    private String scopeType;

    private BigInteger scopeId;

    /** day, week, month */
    private String period;

    @NotNull(message = "预算金额不能为空")
    private BigDecimal amount;

    private BigDecimal alertThreshold;

    private Integer enableStatus;

    private String remark;
}
