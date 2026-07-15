package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiPromptPresetVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String description;

    private String category;

    private String content;

    private String icon;

    private Integer isBuiltin;

    private Integer enableStatus;

    private Integer sort;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
