package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiPromptPresetAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "预设名称不能为空")
    private String name;

    private String description;

    private String category;

    @NotBlank(message = "提示词内容不能为空")
    private String content;

    private String icon;

    private Integer enableStatus;

    private Integer sort;
}
