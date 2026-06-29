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
@Entity(tableName = "ai_agent_log", comment = "AI智能体运行日志表", pk_constraint = "PRIMARY")
public class AiAgentLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "agent_id", comment = "智能体ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger agentId;

    @Column(name = "agent_name", comment = "智能体名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = true)
    private String agentName;

    @Column(name = "conversation_id", comment = "对话ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger conversationId;

    @Column(name = "user_id", comment = "用户ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger userId;

    @Column(name = "user_input", comment = "用户输入", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String userInput;

    @Column(name = "final_response", comment = "最终回复", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String finalResponse;

    @Column(name = "iteration_count", comment = "迭代次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer iterationCount;

    @Column(name = "tool_calls", comment = "工具调用记录(JSON)", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String toolCalls;

    @Column(name = "prompt_tokens", comment = "输入token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer promptTokens;

    @Column(name = "completion_tokens", comment = "输出token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer completionTokens;

    @Column(name = "total_tokens", comment = "总token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer totalTokens;

    @Column(name = "cost", comment = "总花费(元)", length = 12L, type = java.sql.Types.DECIMAL, nullable = true, defaultValue = "0")
    private BigDecimal cost;

    @Column(name = "status", comment = "运行状态;success, error, timeout", length = 20L, defaultValue = "success", type = java.sql.Types.VARCHAR, nullable = true)
    private String status;

    @Column(name = "error_msg", comment = "错误信息", length = 2000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String errorMsg;

    @Column(name = "execution_time_ms", comment = "执行耗时(毫秒)", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = true)
    private Long executionTimeMs;

    @Column(name = "created_time", comment = "创建时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime createdTime;

    public AiAgentLog() {}

    public AiAgentLog(BigInteger id) {
        this.id = id;
    }
}
