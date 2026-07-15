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
@Entity(tableName = "ai_prompt_template_version", comment = "AI提示词模板版本历史表", pk_constraint = "PRIMARY")
public class AiPromptTemplateVersion implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "template_id", comment = "模板ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger templateId;

    @Column(name = "version", comment = "版本号", length = 10L, type = java.sql.Types.INTEGER, nullable = false)
    private Integer version;

    @Column(name = "content", comment = "当次版本内容", length = 65535L, type = java.sql.Types.VARCHAR, nullable = false)
    private String content;

    @Column(name = "variables", comment = "变量列表(JSON)", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String variables;

    @Column(name = "change_note", comment = "变更说明", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String changeNote;

    @Column(name = "create_by", comment = "创建人", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger createBy;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    public AiPromptTemplateVersion() {}

    public AiPromptTemplateVersion(BigInteger id) {
        this.id = id;
    }
}
