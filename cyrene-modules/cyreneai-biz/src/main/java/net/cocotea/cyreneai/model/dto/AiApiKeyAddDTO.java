package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiApiKeyAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Key 名称不能为空")
    private String name;

    private String description;

    /** 允许的模型ID列表(逗号分隔;空=全部) */
    private String allowedModelIds;

    /** 允许调用的IP白名单(逗号分隔) */
    private String allowedIpList;

    private Integer rpmLimit;

    private Integer tpmLimit;

    private Long monthlyTokenQuota;

    private LocalDateTime expireTime;

    private Integer enableStatus;

    private Integer sort;
}
