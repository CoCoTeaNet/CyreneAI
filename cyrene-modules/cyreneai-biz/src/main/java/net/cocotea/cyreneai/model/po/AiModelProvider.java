package net.cocotea.cyreneai.model.po;

import lombok.Data;
import lombok.experimental.Accessors;
import org.sagacity.sqltoy.config.annotation.Column;
import org.sagacity.sqltoy.config.annotation.Entity;
import org.sagacity.sqltoy.config.annotation.Id;

import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
@Entity(tableName = "ai_model_provider", comment = "AI模型提供商表", pk_constraint = "PRIMARY")
public class AiModelProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "提供商id", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "provider_name", comment = "提供商名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String providerName;

    @Column(name = "provider_type", comment = "提供商类型", length = 50L, type = java.sql.Types.VARCHAR, nullable = false)
    private String providerType;

    @Column(name = "api_base_url", comment = "API地址", length = 255L, type = java.sql.Types.VARCHAR, nullable = true)
    private String apiBaseUrl;

    @Column(name = "api_key", comment = "API密钥", length = 512L, type = java.sql.Types.VARCHAR, nullable = true)
    private String apiKey;

    @Column(name = "sort", comment = "排序号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer sort;

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

    public AiModelProvider() {}

    public AiModelProvider(BigInteger id) {
        this.id = id;
    }
}
