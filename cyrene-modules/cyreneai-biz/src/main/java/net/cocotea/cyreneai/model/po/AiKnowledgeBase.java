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
@Entity(tableName = "ai_knowledge_base", comment = "知识库表", pk_constraint = "PRIMARY")
public class AiKnowledgeBase implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "name", comment = "知识库名称", length = 200L, type = java.sql.Types.VARCHAR, nullable = false)
    private String name;

    @Column(name = "description", comment = "知识库描述", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String description;

    @Column(name = "model_id", comment = "关联模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "embedding_model_id", comment = "嵌入模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger embeddingModelId;

    @Column(name = "chunk_size", comment = "默认分块大小", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkSize;

    @Column(name = "chunk_overlap", comment = "默认分块重叠", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer chunkOverlap;

    @Column(name = "chunk_strategy", comment = "默认分块策略", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String chunkStrategy;

    @Column(name = "retrieval_strategy", comment = "检索策略", length = 50L, type = java.sql.Types.VARCHAR, nullable = true)
    private String retrievalStrategy;

    @Column(name = "top_k", comment = "检索返回条数", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer topK;

    @Column(name = "similarity_threshold", comment = "相似度阈值", length = 5L, type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal similarityThreshold;

    @Column(name = "enable_status", comment = "启用状态;0关闭 1启用", length = 3L, defaultValue = "1", type = java.sql.Types.TINYINT, nullable = true)
    private Integer enableStatus;

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

    public AiKnowledgeBase() {}

    public AiKnowledgeBase(BigInteger id) {
        this.id = id;
    }
}
