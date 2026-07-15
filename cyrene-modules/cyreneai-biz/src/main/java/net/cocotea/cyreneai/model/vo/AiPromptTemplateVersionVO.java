package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class AiPromptTemplateVersionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private BigInteger templateId;

    private Integer version;

    private String content;

    private String variables;

    private String changeNote;

    private String createBy;

    private LocalDateTime createTime;
}
