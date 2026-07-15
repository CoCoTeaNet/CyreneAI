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
@Entity(tableName = "ai_api_key", comment = "AI API Key 表", pk_constraint = "PRIMARY")
public class AiApiKey implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "user_id", comment = "所属用户ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger userId;

    @Column(name = "name", comment = "Key 名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "description", comment = "备注", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String description;

    @Column(name = "key_hash", comment = "Key 的 SHA-256 哈希", length = 128L, type = java.sql.Types.VARCHAR, nullable = false)
    private String keyHash;

    @Column(name = "key_prefix", comment = "Key 前缀(用于展示)", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String keyPrefix;

    @Column(name = "allowed_model_ids", comment = "允许使用的模型ID列表(逗号分隔)", length = 1000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String allowedModelIds;

    @Column(name = "allowed_ip_list", comment = "IP白名单", length = 1000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String allowedIpList;

    @Column(name = "rpm_limit", comment = "每分钟请求数限制", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer rpmLimit;

    @Column(name = "tpm_limit", comment = "每分钟Token数限制", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer tpmLimit;

    @Column(name = "monthly_token_quota", comment = "月度Token配额", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private Long monthlyTokenQuota;

    @Column(name = "tokens_used_this_month", comment = "本月已用Token数", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = true)
    private Long tokensUsedThisMonth;

    @Column(name = "quota_reset_time", comment = "配额下次重置时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime quotaResetTime;

    @Column(name = "expire_time", comment = "过期时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime expireTime;

    @Column(name = "last_used_time", comment = "最近使用时间", length = 19L, type = java.sql.Types.DATE, nullable = true)
    private LocalDateTime lastUsedTime;

    @Column(name = "enable_status", comment = "启用状态", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

    @Column(name = "sort", comment = "排序号", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
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

    public AiApiKey() {}

    public AiApiKey(BigInteger id) {
        this.id = id;
    }
}
