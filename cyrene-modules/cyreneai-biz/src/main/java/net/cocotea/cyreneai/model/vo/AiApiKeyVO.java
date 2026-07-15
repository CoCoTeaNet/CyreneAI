package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiApiKeyVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private BigInteger userId;

    private String userName;

    private String name;

    private String description;

    private String keyPrefix;

    private String allowedModelIds;

    private String allowedIpList;

    private Integer rpmLimit;

    private Integer tpmLimit;

    private Long monthlyTokenQuota;

    private Long tokensUsedThisMonth;

    private LocalDateTime quotaResetTime;

    private LocalDateTime expireTime;

    private LocalDateTime lastUsedTime;

    private Integer enableStatus;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 仅生成时返回一次的明文 Key */
    private String plainKey;
}
