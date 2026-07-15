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
@Entity(tableName = "ai_prompt_ab_test", comment = "AI提示词A/B测试表", pk_constraint = "PRIMARY")
public class AiPromptAbTest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "测试名称", length = 200L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "description", comment = "测试描述", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String description;

    @Column(name = "template_a_id", comment = "版本A模板ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger templateAId;

    @Column(name = "template_a_version", comment = "版本A的版本号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer templateAVersion;

    @Column(name = "template_b_id", comment = "版本B模板ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger templateBId;

    @Column(name = "template_b_version", comment = "版本B的版本号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer templateBVersion;

    @Column(name = "model_id", comment = "模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "traffic_split", comment = "流量分配百分比(A侧)", length = 10L, defaultValue = "50", type = java.sql.Types.INTEGER, nullable = true)
    private Integer trafficSplit;

    @Column(name = "status", comment = "状态;draft, running, finished", length = 20L, defaultValue = "running", type = java.sql.Types.VARCHAR, nullable = true)
    private String status;

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

    public AiPromptAbTest() {}

    public AiPromptAbTest(BigInteger id) {
        this.id = id;
    }
}
