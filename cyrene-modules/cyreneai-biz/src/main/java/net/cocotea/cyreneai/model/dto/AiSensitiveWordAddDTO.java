package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiSensitiveWordAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "敏感词不能为空")
    private String word;

    private String category;

    /** block, replace, warn */
    @NotBlank(message = "策略不能为空")
    private String strategy;

    private String replacement;

    /** input, output, both */
    private String target;

    private Integer enableStatus;

    private Integer sort;
}
