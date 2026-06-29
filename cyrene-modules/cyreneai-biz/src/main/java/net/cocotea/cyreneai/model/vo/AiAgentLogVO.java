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
public class AiAgentLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private BigInteger agentId;

    private String agentName;

    private BigInteger conversationId;

    private BigInteger userId;

    private String userInput;

    private String finalResponse;

    private Integer iterationCount;

    private String toolCalls;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private BigDecimal cost;

    private String status;

    private String errorMsg;

    private Long executionTimeMs;

    private LocalDateTime createdTime;
}
