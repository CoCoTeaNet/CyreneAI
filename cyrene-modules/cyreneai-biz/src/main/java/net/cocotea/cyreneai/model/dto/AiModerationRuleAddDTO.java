package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class AiModerationRuleAddDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "规则名称不能为空")
    private String name;

    /** openai_moderation, dashscope, sensitive_word, keyword_regex */
    @NotBlank(message = "提供者不能为空")
    private String provider;

    private String configJson;

    private BigDecimal threshold;

    /** block, replace, warn, pass */
    private String action;

    /** input, output, both */
    private String target;

    private Integer sort;

    private Integer enableStatus;
}
