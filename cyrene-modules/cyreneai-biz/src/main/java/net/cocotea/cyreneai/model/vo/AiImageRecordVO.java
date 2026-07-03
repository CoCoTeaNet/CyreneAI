package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiImageRecordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;
    private String prompt;
    private String revisedPrompt;
    private String modelName;
    private String imageUrl;
    private String imageSize;
    private String style;
    private BigDecimal cost;
    private BigInteger createBy;
    private LocalDateTime createTime;
    private BigInteger updateBy;
    private LocalDateTime updateTime;
}
