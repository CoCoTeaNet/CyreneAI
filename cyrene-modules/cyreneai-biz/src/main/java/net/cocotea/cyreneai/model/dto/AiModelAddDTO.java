package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class AiModelAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "提供商不能为空")
    private String providerId;

    @NotBlank(message = "模型名称不能为空")
    private String modelName;

    private Integer contextWindow;

    private BigDecimal inputPrice;

    private BigDecimal outputPrice;

    private Integer isDefault;

    private Integer sort;

    private Integer enableStatus;

    private String remark;
}
