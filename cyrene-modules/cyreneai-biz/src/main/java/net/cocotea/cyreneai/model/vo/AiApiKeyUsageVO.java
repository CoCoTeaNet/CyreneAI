package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;

@Data
@Accessors(chain = true)
public class AiApiKeyUsageVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger apiKeyId;

    private BigInteger userId;

    private LocalDate statDate;

    private Integer requestCount;

    private Integer successCount;

    private Integer blockedCount;

    private Integer errorCount;

    private Long promptTokens;

    private Long completionTokens;

    private Long totalTokens;

    private BigDecimal cost;
}
