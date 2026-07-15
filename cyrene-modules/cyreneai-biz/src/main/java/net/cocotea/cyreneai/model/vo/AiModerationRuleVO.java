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
public class AiModerationRuleVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String provider;

    private String configJson;

    private BigDecimal threshold;

    private String action;

    private String target;

    private Integer sort;

    private Integer enableStatus;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
