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
@Entity(tableName = "ai_moderation_rule", comment = "AI 内容审核规则表", pk_constraint = "PRIMARY")
public class AiModerationRule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "规则名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "provider", comment = "提供者", length = 50L, type = java.sql.Types.VARCHAR, nullable = false)
    private String provider;

    @Column(name = "config_json", comment = "规则配置(JSON)", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String configJson;

    @Column(name = "threshold", comment = "分数阈值", length = 6L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal threshold;

    @Column(name = "action", comment = "命中动作;block, replace, warn, pass", length = 20L, defaultValue = "block", type = java.sql.Types.VARCHAR, nullable = false)
    private String action;

    @Column(name = "target", comment = "作用目标;input, output, both", length = 20L, defaultValue = "both", type = java.sql.Types.VARCHAR, nullable = false)
    private String target;

    @Column(name = "sort", comment = "排序号(数值大优先)", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer sort;

    @Column(name = "enable_status", comment = "启用状态", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

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

    public AiModerationRule() {}

    public AiModerationRule(BigInteger id) {
        this.id = id;
    }
}
