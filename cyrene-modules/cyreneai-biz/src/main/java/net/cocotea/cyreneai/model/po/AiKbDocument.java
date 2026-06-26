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
@Entity(tableName = "ai_kb_document", comment = "知识库文档关联表", pk_constraint = "PRIMARY")
public class AiKbDocument implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "kb_id", comment = "知识库ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger kbId;

    @Column(name = "document_id", comment = "文档ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger documentId;

    @Column(name = "create_time", comment = "创建时间", type = java.sql.Types.TIMESTAMP, nullable = true)
    private LocalDateTime createTime;

    public AiKbDocument() {}

    public AiKbDocument(BigInteger id) {
        this.id = id;
    }
}
