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
@Entity(tableName = "ai_prompt_preset", comment = "AI系统提示词预设表", pk_constraint = "PRIMARY")
public class AiPromptPreset implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "预设名称", length = 200L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "description", comment = "预设描述", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String description;

    @Column(name = "category", comment = "分类", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String category;

    @Column(name = "content", comment = "提示词内容", length = 65535L, type = java.sql.Types.VARCHAR, nullable = false)
    private String content;

    @Column(name = "icon", comment = "图标", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String icon;

    @Column(name = "is_builtin", comment = "是否内置;0否 1是", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = true)
    private Integer isBuiltin;

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

    public AiPromptPreset() {}

    public AiPromptPreset(BigInteger id) {
        this.id = id;
    }
}
