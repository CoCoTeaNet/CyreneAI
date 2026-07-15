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
@Entity(tableName = "ai_prompt_eval", comment = "AI提示词效果评估记录表", pk_constraint = "PRIMARY")
public class AiPromptEval implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id(strategy = "generator", generator = "org.sagacity.sqltoy.plugins.id.impl.SnowflakeIdGenerator")
    @Column(name = "id", comment = "主键ID", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger id;

    @Column(name = "template_id", comment = "模板ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger templateId;

    @Column(name = "template_version", comment = "模板版本号", length = 10L, type = java.sql.Types.INTEGER, nullable = true)
    private Integer templateVersion;

    @Column(name = "model_id", comment = "模型ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger modelId;

    @Column(name = "model_name", comment = "模型名称", length = 100L, type = java.sql.Types.VARCHAR, nullable = true)
    private String modelName;

    @Column(name = "ab_test_id", comment = "关联A/B测试ID", length = 19L, type = java.sql.Types.BIGINT, nullable = true)
    private BigInteger abTestId;

    @Column(name = "variant", comment = "A/B测试分组;A, B", length = 10L, type = java.sql.Types.VARCHAR, nullable = true)
    private String variant;

    @Column(name = "input_variables", comment = "输入变量(JSON)", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String inputVariables;

    @Column(name = "rendered_prompt", comment = "渲染后的提示词", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String renderedPrompt;

    @Column(name = "output", comment = "模型输出", length = 65535L, type = java.sql.Types.VARCHAR, nullable = true)
    private String output;

    @Column(name = "prompt_tokens", comment = "输入token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer promptTokens;

    @Column(name = "completion_tokens", comment = "输出token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer completionTokens;

    @Column(name = "total_tokens", comment = "总token数", length = 10L, defaultValue = "0", type = java.sql.Types.INTEGER, nullable = true)
    private Integer totalTokens;

    @Column(name = "cost", comment = "本次花费(元)", length = 18L, defaultValue = "0", type = java.sql.Types.DECIMAL, nullable = true)
    private BigDecimal cost;

    @Column(name = "latency_ms", comment = "执行耗时(毫秒)", length = 19L, defaultValue = "0", type = java.sql.Types.BIGINT, nullable = true)
    private Long latencyMs;

    @Column(name = "rating", comment = "效果评分(1-5)", length = 3L, type = java.sql.Types.TINYINT, nullable = true)
    private Integer rating;

    @Column(name = "feedback", comment = "评价反馈", length = 500L, type = java.sql.Types.VARCHAR, nullable = true)
    private String feedback;

    @Column(name = "create_by", comment = "创建人", length = 19L, type = java.sql.Types.BIGINT, nullable = false)
    private BigInteger createBy;

    @Column(name = "create_time", comment = "创建时间", length = 19L, type = java.sql.Types.DATE, nullable = false)
    private LocalDateTime createTime;

    public AiPromptEval() {}

    public AiPromptEval(BigInteger id) {
        this.id = id;
    }
}
