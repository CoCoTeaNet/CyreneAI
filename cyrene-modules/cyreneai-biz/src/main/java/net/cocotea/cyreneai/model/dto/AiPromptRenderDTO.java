package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AiPromptRenderDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID(可选，与 content 二选一)
     */
    private String templateId;

    /**
     * 模板版本号(可选，为空使用当前版本)
     */
    private Integer version;

    /**
     * 直接传入模板内容(可选，与 templateId 二选一)
     */
    private String content;

    /**
     * 变量键值对
     */
    private Map<String, Object> variables;
}
