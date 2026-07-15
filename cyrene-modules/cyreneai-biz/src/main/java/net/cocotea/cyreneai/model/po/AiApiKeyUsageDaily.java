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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName = "ai_api_key_usage_daily", comment = "AI API Key 每日调用统计", pk_constraint = "PRIMARY")
public class AiApiKeyUsageDaily implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "api_key_id", comment = "API Key ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger apiKeyId;

    @Column(name = "user_id", comment = "用户ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger userId;

    @Column(name = "stat_date", comment = "统计日期", length = 10L, type = java.sql.Types.DATE, nullable = false)
    private LocalDate statDate;

    @Column(name = "request_count", comment = "请求次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = false)
    private Integer requestCount;

    @Column(name = "success_count", comment = "成功次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = false)
    private Integer successCount;

    @Column(name = "blocked_count", comment = "被拦截次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = false)
    private Integer blockedCount;

    @Column(name = "error_count", comment = "异常次数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = false)
    private Integer errorCount;

    @Column(name = "prompt_tokens", comment = "输入Token数", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = false)
    private Long promptTokens;

    @Column(name = "completion_tokens", comment = "输出Token数", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = false)
    private Long completionTokens;

    @Column(name = "total_tokens", comment = "总Token数", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = false)
    private Long totalTokens;

    @Column(name = "cost", comment = "当日花费(元)", length = 14L, defaultValue = "0", type = java.sql.Types.DECIMAL, nullable = false)
    private BigDecimal cost;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    @Column(name = "update_time", comment = "更新时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime updateTime;

    public AiApiKeyUsageDaily() {}

    public AiApiKeyUsageDaily(BigInteger id) {
        this.id = id;
    }
}
