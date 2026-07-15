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
@Entity(tableName = "ai_quota_alert", comment = "AI 配额告警配置表", pk_constraint = "PRIMARY")
public class AiQuotaAlert implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "告警名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "scope", comment = "作用范围;global 全局, key 单Key", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String scope;

    @Column(name = "api_key_id", comment = "关联API Key ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger apiKeyId;

    @Column(name = "metric", comment = "监控指标;monthly_tokens, daily_cost, error_rate", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String metric;

    @Column(name = "threshold_percent", comment = "阈值百分比", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer thresholdPercent;

    @Column(name = "threshold_value", comment = "阈值绝对值", length = 14L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal thresholdValue;

    @Column(name = "notify_channel", comment = "通知渠道;system, email, webhook", length = 50L, defaultValue = "system", type = java.sql.Types.VARCHAR, nullable = true)
    private String notifyChannel;

    @Column(name = "notify_target", comment = "通知目标", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String notifyTarget;

    @Column(name = "last_triggered_time", comment = "最近触发时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime lastTriggeredTime;

    @Column(name = "trigger_count", comment = "累计触发次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = false)
    private Integer triggerCount;

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

    public AiQuotaAlert() {}

    public AiQuotaAlert(BigInteger id) {
        this.id = id;
    }
}
