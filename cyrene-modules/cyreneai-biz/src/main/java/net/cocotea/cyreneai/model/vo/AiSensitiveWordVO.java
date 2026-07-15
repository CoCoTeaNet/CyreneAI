package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiSensitiveWordVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String word;

    private String category;

    private String strategy;

    private String replacement;

    private String target;

    private Integer enableStatus;

    private Integer sort;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
