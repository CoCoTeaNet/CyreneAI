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
@Entity(tableName = "ai_image_record", comment = "图片生成记录表", pk_constraint = "PRIMARY")
public class AiImageRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "prompt", comment = "提示词", length = 4000L, type = java.sql.Types.VARCHAR, nullable = false)
    private String prompt;

    @Column(name = "revised_prompt", comment = "优化后的提示词", length = 4000L, type = java.sql.Types.VARCHAR, nullable = true)
    private String revisedPrompt;

    @Column(name = "model_name", comment = "模型名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = false)
    private String modelName;

    @Column(name = "image_url", comment = "图片URL", length = 1024L, type = java.sql.Types.VARCHAR, nullable = false)
    private String imageUrl;

    @Column(name = "image_size", comment = "图片尺寸", length = 20L, type = java.sql.Types.VARCHAR, nullable = true)
    private String imageSize;

    @Column(name = "style", comment = "图片风格", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String style;

    @Column(name = "cost", comment = "消耗额度", length = 10L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal cost;

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

    public AiImageRecord() {}

    public AiImageRecord(BigInteger id) {
        this.id = id;
    }
}
