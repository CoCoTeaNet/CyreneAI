package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiPromptAbTestAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "测试名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "版本A模板不能为空")
    private String templateAId;

    private Integer templateAVersion;

    @NotNull(message = "版本B模板不能为空")
    private String templateBId;

    private Integer templateBVersion;

    private String modelId;

    private Integer trafficSplit;

    private String status;
}
