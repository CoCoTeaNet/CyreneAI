package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
@Accessors(chain = true)
public class AiPromptEvalRunDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID(可选，与 promptContent 二选一)
     */
    private String templateId;

    /**
     * 使用的模板版本号(可选，为空使用当前版本)
     */
    private Integer version;

    /**
     * 直接传入渲染后的 prompt 内容(可选，优先使用)
     */
    private String promptContent;

    /**
     * 变量键值对
     */
    private Map<String, Object> variables;

    @NotNull(message = "模型不能为空")
    private String modelId;

    /**
     * A/B 测试 ID (可选)
     */
    private String abTestId;

    /**
     * 分组标识 A / B (可选)
     */
    private String variant;
}
