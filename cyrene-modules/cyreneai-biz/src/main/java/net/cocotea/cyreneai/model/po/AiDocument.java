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
@Entity(tableName = "ai_document", comment = "文档表", pk_constraint = "PRIMARY")
public class AiDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "文档名称", length = 255L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "type", comment = "文档类型", length = 20L, type = java.sql.Types.VARCHAR, nullable = false)
    private String type;

    @Column(name = "size", comment = "文件大小(字节)", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private Long size;

    @Column(name = "file_path", comment = "文件存储路径", length = 512L, type = java.sql.Types.VARCHAR, nullable = true)
    private String filePath;

    @Column(name = "status", comment = "处理状态;0待处理 1处理中 2已完成 3失败", length = 3L, defaultValue = "0", type = java.sql.Types.TINYINT, nullable = true)
    private Integer status;

    @Column(name = "chunk_count", comment = "分块数量", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkCount;

    @Column(name = "chunk_strategy", comment = "分块策略", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String chunkStrategy;

    @Column(name = "chunk_size", comment = "分块大小", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkSize;

    @Column(name = "chunk_overlap", comment = "分块重叠", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkOverlap;

    @Column(name = "error_msg", comment = "错误信息", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String errorMsg;

    @Column(name = "kb_id", comment = "所属知识库ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger kbId;

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

    public AiDocument() {}

    public AiDocument(BigInteger id) {
        this.id = id;
    }
}
