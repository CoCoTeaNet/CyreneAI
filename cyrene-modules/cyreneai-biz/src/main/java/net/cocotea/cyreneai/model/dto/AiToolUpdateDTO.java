package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiToolUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "主键ID为空")
    private String id;

    private String name;

    private String description;

    private String type;

    private String schemaJson;

    private String url;

    private String authType;

    private String authValue;

    private String httpMethod;

    private String builtinHandler;

    private Integer enableStatus;

    private Integer sort;
}
