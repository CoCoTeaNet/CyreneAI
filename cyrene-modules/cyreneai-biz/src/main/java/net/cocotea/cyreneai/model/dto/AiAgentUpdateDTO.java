package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@Accessors(chain = true)
public class AiAgentUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "主键ID为空")
    private String id;

    private String name;

    private String description;

    private String modelId;

    private String systemPrompt;

    private List<String> toolIds;

    private Integer maxIterations;

    private BigDecimal temperature;

    private BigDecimal topP;

    private Integer maxTokens;

    private Integer enableStatus;

    private Integer sort;
}
