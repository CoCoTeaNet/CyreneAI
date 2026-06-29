package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Accessors(chain = true)
public class AiAgentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String description;

    private BigInteger modelId;

    private String modelName;

    private String systemPrompt;

    private String toolIds;

    private List<AiToolVO> tools;

    private Integer maxIterations;

    private BigDecimal temperature;

    private BigDecimal topP;

    private Integer maxTokens;

    private Integer enableStatus;

    private Integer sort;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
