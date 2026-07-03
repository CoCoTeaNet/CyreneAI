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
@Entity(tableName = "ai_vision_model", comment = "视觉模型配置表", pk_constraint = "PRIMARY")
public class AiVisionModel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "provider_type", comment = "提供商类型", length = 50L, type = java.sql.Types.VARCHAR, nullable = false)
    private String providerType;

    @Column(name = "model_name", comment = "模型名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String modelName;

    @Column(name = "api_key", comment = "API密钥", length = 512L, type = java.sql.Types.VARCHAR, nullable = true)
    private String apiKey;

    @Column(name = "api_base_url", comment = "API地址", length = 255L, type = java.sql.Types.VARCHAR, nullable = true)
    private String apiBaseUrl;

    @Column(name = "is_default", comment = "是否默认;0否 1是", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = true)
    private Integer isDefault;

    @Column(name = "enable_status", comment = "启用状态;0关闭 1启用", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

    @Column(name = "sort", comment = "排序号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer sort;

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

    public AiVisionModel() {}

    public AiVisionModel(BigInteger id) {
        this.id = id;
    }
}
