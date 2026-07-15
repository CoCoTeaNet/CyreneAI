package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiPromptAbTestVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String description;

    private BigInteger templateAId;

    private String templateAName;

    private Integer templateAVersion;

    private BigInteger templateBId;

    private String templateBName;

    private Integer templateBVersion;

    private BigInteger modelId;

    private String modelName;

    private Integer trafficSplit;

    private String status;

    /** 累计样本数(A) */
    private Long sampleCountA;

    /** 累计样本数(B) */
    private Long sampleCountB;

    /** 平均评分(A) */
    private Double avgRatingA;

    /** 平均评分(B) */
    private Double avgRatingB;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
