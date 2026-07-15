package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class AiPromptPresetPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "查询参数为空")
    private Query aiPromptPreset;

    @Data
    public static class Query {

        private String name;

        private String category;

        private Integer enableStatus;
    }
}
