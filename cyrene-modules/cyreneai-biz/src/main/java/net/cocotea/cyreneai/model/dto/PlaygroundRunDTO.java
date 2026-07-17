package net.cocotea.cyreneai.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * Playground / 模型对比运行请求
 */
@Data
@Accessors(chain = true)
public class PlaygroundRunDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 参与对比的模型ID列表(1个即普通测试, 多个即并排对比) */
    @NotEmpty(message = "请至少选择一个模型")
    private List<BigInteger> modelIds;

    @NotBlank(message = "提示词不能为空")
    private String prompt;

    private String systemPrompt;

    private Double temperature;

    private Integer maxTokens;
}
