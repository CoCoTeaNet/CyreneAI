package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 用户调用排行
 */
@Data
@Accessors(chain = true)
public class AiUserRankVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger userId;
    private String userName;
    private Long requestCount;
    private Long totalTokens;
    private BigDecimal cost;
}
