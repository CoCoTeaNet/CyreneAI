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
@Entity(tableName = "ai_budget", comment = "AI 成本预算表", pk_constraint = "PRIMARY")
public class AiBudget implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "预算名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "scope_type", comment = "范围类型;global, model, user", length = 20L, defaultValue = "global", type = java.sql.Types.VARCHAR, nullable = false)
    private String scopeType;

    @Column(name = "scope_id", comment = "范围对象ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger scopeId;

    @Column(name = "period", comment = "统计周期;day, week, month", length = 20L, defaultValue = "month", type = java.sql.Types.VARCHAR, nullable = false)
    private String period;

    @Column(name = "amount", comment = "预算金额(元)", length = 12L, type = java.sql.Types.DECIMAL, nullable = false)
    private BigDecimal amount;

    @Column(name = "alert_threshold", comment = "告警阈值(0-1)", length = 5L, defaultValue = "0.8", type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal alertThreshold;

    @Column(name = "enable_status", comment = "启用状态;0关闭 1启用", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

    @Column(name = "remark", comment = "备注", length = 255L, type = java.sql.Types.VARCHAR, nullable = true)
    private String remark;

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

    public AiBudget() {}

    public AiBudget(BigInteger id) {
        this.id = id;
    }
}
