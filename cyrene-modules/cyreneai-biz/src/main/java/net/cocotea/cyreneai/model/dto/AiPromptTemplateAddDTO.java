package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiPromptTemplateAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "模板名称不能为空")
    private String name;

    private String description;

    private String category;

    private String scene;

    @NotBlank(message = "模板内容不能为空")
    private String content;

    private String variables;

    private Integer enableStatus;

    private Integer sort;

    /**
     * 首次创建版本时的变更说明
     */
    private String changeNote;
}
