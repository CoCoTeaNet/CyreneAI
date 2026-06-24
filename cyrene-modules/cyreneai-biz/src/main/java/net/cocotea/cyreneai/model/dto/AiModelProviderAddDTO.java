package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class AiModelProviderAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "提供商名称不能为空")
    private String providerName;

    @NotBlank(message = "提供商类型不能为空")
    private String providerType;

    private String apiBaseUrl;

    private String apiKey;

    private Integer sort;

    private Integer enableStatus;

    private String remark;
}
