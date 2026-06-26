package net.cocotea.cyreneai.model.po;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName = "ai_message", comment = "AI消息表", pk_constraint = "PRIMARY")
public class AiMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "conversation_id", comment = "对话ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger conversationId;

    @Column(name = "role", comment = "角色: user/assistant/system", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String role;

    @Column(name = "content", comment = "消息内容", length = 65535L, type = java.sql.Types.VARCHAR, nullable = false)
    private String content;

    @Column(name = "prompt_tokens", comment = "输入token数", length = 10L, type = java.sql.Types.INTEGER, nullable = true, defaultValue = "0")
    private Integer promptTokens;

    @Column(name = "completion_tokens", comment = "输出token数", length = 10L, type = java.sql.Types.INTEGER, nullable = true, defaultValue = "0")
    private Integer completionTokens;

    @Column(name = "total_tokens", comment = "总token数", length = 10L, type = java.sql.Types.INTEGER, nullable = true, defaultValue = "0")
    private Integer totalTokens;

    @Column(name = "created_time", comment = "创建时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime createdTime;

    public AiMessage() {}

    public AiMessage(BigInteger id) {
        this.id = id;
    }
}
