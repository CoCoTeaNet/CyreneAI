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
public class AiAuditLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private BigInteger userId;

    private String userName;

    private BigInteger apiKeyId;

    private String apiKeyName;

    private String endpoint;

    private String httpMethod;

    private BigInteger modelId;

    private String modelName;

    private String providerType;

    private BigInteger conversationId;

    private String requestId;

    private String promptSnippet;

    private String outputSnippet;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private BigDecimal cost;

    private Long latencyMs;

    private String status;

    private String errorMsg;

    private String ip;

    private String userAgent;

    private LocalDateTime createTime;
}
