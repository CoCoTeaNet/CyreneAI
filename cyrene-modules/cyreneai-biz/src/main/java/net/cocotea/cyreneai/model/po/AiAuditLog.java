package net.cocotea.cyreneai.model.po;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName = "ai_audit_log", comment = "AI 请求审计日志表", pk_constraint = "PRIMARY")
public class AiAuditLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "user_id", comment = "用户ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger userId;

    @Column(name = "api_key_id", comment = "API Key ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger apiKeyId;

    @Column(name = "endpoint", comment = "端点路径", length = 200L, type = java.sql.Types.VARCHAR, nullable = false)
    private String endpoint;

    @Column(name = "http_method", comment = "HTTP方法", length = 10L, type = java.sql.Types.VARCHAR, nullable = true)
    private String httpMethod;

    @Column(name = "model_id", comment = "模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "model_name", comment = "模型名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = true)
    private String modelName;

    @Column(name = "provider_type", comment = "提供商类型", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String providerType;

    @Column(name = "conversation_id", comment = "会话ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger conversationId;

    @Column(name = "request_id", comment = "请求追踪ID", length = 64L, type = java.sql.Types.VARCHAR, nullable = true)
    private String requestId;

    @Column(name = "prompt_snippet", comment = "输入摘要", length = 1000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String promptSnippet;

    @Column(name = "output_snippet", comment = "输出摘要", length = 1000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String outputSnippet;

    @Column(name = "prompt_tokens", comment = "输入Token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer promptTokens;

    @Column(name = "completion_tokens", comment = "输出Token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer completionTokens;

    @Column(name = "total_tokens", comment = "总Token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer totalTokens;

    @Column(name = "cost", comment = "花费(元)", length = 12L, defaultValue = "0", type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal cost;

    @Column(name = "latency_ms", comment = "耗时(毫秒)", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = true)
    private Long latencyMs;

    @Column(name = "status", comment = "状态", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String status;

    @Column(name = "error_msg", comment = "错误信息", length = 1000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String errorMsg;

    @Column(name = "ip", comment = "来源IP", length = 64L, type = java.sql.Types.VARCHAR, nullable = true)
    private String ip;

    @Column(name = "user_agent", comment = "User-Agent", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String userAgent;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    public AiAuditLog() {}

    public AiAuditLog(BigInteger id) {
        this.id = id;
    }
}
