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
@Entity(tableName = "ai_conversation", comment = "AI对话表", pk_constraint = "PRIMARY")
public class AiConversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "title", comment = "对话标题", length = 200L, type = java.sql.Types.VARCHAR, nullable = true)
    private String title;

    @Column(name = "user_id", comment = "用户ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger userId;

    @Column(name = "model_id", comment = "模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "system_prompt", comment = "系统提示词", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String systemPrompt;

    @Column(name = "created_time", comment = "创建时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime createdTime;

    @Column(name = "updated_time", comment = "更新时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime updatedTime;

    @Column(name = "is_deleted", comment = "是否删除", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = true)
    private Integer isDeleted;

    public AiConversation() {}

    public AiConversation(BigInteger id) {
        this.id = id;
    }
}
