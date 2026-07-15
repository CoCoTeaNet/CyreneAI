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
@Entity(tableName = "ai_sensitive_word", comment = "AI 敏感词表", pk_constraint = "PRIMARY")
public class AiSensitiveWord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "word", comment = "敏感词", length = 200L, type = java.sql.Types.VARCHAR, nullable = false)
    private String word;

    @Column(name = "category", comment = "分类", length = 50L, defaultValue = "custom", type = java.sql.Types.VARCHAR, nullable = true)
    private String category;

    @Column(name = "strategy", comment = "策略;block, replace, warn", length = 20L, defaultValue = "block", type = java.sql.Types.VARCHAR, nullable = false)
    private String strategy;

    @Column(name = "replacement", comment = "替换文本", length = 200L, defaultValue = "***", type = java.sql.Types.VARCHAR, nullable = true)
    private String replacement;

    @Column(name = "target", comment = "作用目标;input, output, both", length = 20L, defaultValue = "both", type = java.sql.Types.VARCHAR, nullable = false)
    private String target;

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

    public AiSensitiveWord() {}

    public AiSensitiveWord(BigInteger id) {
        this.id = id;
    }
}
