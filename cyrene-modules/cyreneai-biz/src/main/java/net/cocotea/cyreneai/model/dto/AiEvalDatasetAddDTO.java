package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiEvalDatasetAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "数据集名称不能为空")
    private String name;

    private String description;

    private String category;

    /** JSON 数组字符串: [{"prompt":"...","expected":"..."}] */
    private String itemsJson;

    private Integer enableStatus;

    private Integer sort;
}
