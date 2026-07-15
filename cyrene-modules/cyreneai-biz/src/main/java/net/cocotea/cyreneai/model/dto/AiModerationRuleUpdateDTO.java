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
public class AiModerationRuleUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID 不能为空")
    private BigInteger id;

    private String name;

    private String provider;

    private String configJson;

    private BigDecimal threshold;

    private String action;

    private String target;

    private Integer sort;

    private Integer enableStatus;
}
