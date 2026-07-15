package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiPromptAbTestUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "主键ID为空")
    private String id;

    private String name;

    private String description;

    private String templateAId;

    private Integer templateAVersion;

    private String templateBId;

    private Integer templateBVersion;

    private String modelId;

    private Integer trafficSplit;

    private String status;
}
