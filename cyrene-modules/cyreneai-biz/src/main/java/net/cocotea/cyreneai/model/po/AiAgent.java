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
@Entity(tableName = "ai_agent", comment = "AI智能体表", pk_constraint = "PRIMARY")
public class AiAgent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "智能体名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "description", comment = "智能体描述", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String description;

    @Column(name = "model_id", comment = "关联模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "system_prompt", comment = "系统提示词", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String systemPrompt;

    @Column(name = "tool_ids", comment = "关联工具ID列表(JSON数组)", length = 2000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String toolIds;

    @Column(name = "max_iterations", comment = "最大迭代次数", length = 10L, defaultValue = "10", type = java.sql.Types.INTEGER, nullable = true)
    private Integer maxIterations;

    @Column(name = "temperature", comment = "温度参数", length = 5L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal temperature;

    @Column(name = "top_p", comment = "Top-P参数", length = 5L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal topP;

    @Column(name = "max_tokens", comment = "最大输出token数", length = 10L, defaultValue = "2048", type = java.sql.Types.INTEGER, nullable = true)
    private Integer maxTokens;

    @Column(name = "enable_status", comment = "启用状态;0关闭 1启用", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

    @Column(name = "sort", comment = "排序号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer sort;

    @Column(name = "create_by", comment = "创建人", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger createBy;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_by", comment = "更新人", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger updateBy;

    @Column(name = "update_time", comment = "更新时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime updateTime;

    @Column(name = "is_deleted", comment = "是否删除", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = false)
    private Integer isDeleted;

    @Column(name = "revision", comment = "乐观锁", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer revision;

    public AiAgent() {}

    public AiAgent(BigInteger id) {
        this.id = id;
    }
}
