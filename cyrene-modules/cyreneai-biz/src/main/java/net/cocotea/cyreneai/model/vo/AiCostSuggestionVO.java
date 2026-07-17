package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * 成本优化建议
 */
@Data
@Accessors(chain = true)
public class AiCostSuggestionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 级别: info, warning, danger */
    private String level;
    private String title;
    private String detail;

    public AiCostSuggestionVO() {}

    public AiCostSuggestionVO(String level, String title, String detail) {
        this.level = level;
        this.title = title;
        this.detail = detail;
    }
}
