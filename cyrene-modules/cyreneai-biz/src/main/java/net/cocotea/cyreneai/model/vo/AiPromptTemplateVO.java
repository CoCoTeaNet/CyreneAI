package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiPromptTemplateVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String name;

    private String description;

    private String category;

    private String scene;

    private String content;

    private String variables;

    private Integer currentVersion;

    private Integer enableStatus;

    private Integer sort;

    private String createBy;

    private LocalDateTime createTime;

    private String updateBy;

    private LocalDateTime updateTime;
}
