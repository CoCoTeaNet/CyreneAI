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
@Entity(tableName = "ai_document_chunk", comment = "文档分块表", pk_constraint = "PRIMARY")
public class AiDocumentChunk implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "document_id", comment = "文档ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger documentId;

    @Column(name = "kb_id", comment = "知识库ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger kbId;

    @Column(name = "content", comment = "分块内容", length = 65535L, type = java.sql.Types.VARCHAR, nullable = false)
    private String content;

    @Column(name = "embedding", comment = "向量数据(JSON数组)", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String embedding;

    @Column(name = "chunk_index", comment = "分块序号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkIndex;

    @Column(name = "metadata", comment = "元数据", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String metadata;

    @Column(name = "create_time", comment = "创建时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime createTime;

    public AiDocumentChunk() {}

    public AiDocumentChunk(BigInteger id) {
        this.id = id;
    }
}
