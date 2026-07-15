package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiPromptEvalVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private BigInteger templateId;

    private String templateName;

    private Integer templateVersion;

    private BigInteger modelId;

    private String modelName;

    private BigInteger abTestId;

    private String variant;

    private String inputVariables;

    private String renderedPrompt;

    private String output;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private BigDecimal cost;

    private Long latencyMs;

    private Integer rating;

    private String feedback;

    private String createBy;

    private LocalDateTime createTime;
}
