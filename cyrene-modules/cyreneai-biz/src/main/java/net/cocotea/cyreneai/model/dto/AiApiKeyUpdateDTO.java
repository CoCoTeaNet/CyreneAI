package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiApiKeyUpdateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "ID 不能为空")
    private BigInteger id;

    private String name;

    private String description;

    private String allowedModelIds;

    private String allowedIpList;

    private Integer rpmLimit;

    private Integer tpmLimit;

    private Long monthlyTokenQuota;

    private LocalDateTime expireTime;

    private Integer enableStatus;

    private Integer sort;
}
