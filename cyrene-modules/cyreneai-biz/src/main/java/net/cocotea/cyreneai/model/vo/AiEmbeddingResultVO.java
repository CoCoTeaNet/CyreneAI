package net.cocotea.cyreneai.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 文本嵌入结果。
 */
@Data
@Accessors(chain = true)
public class AiEmbeddingResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 使用的模型名称 */
    private String model;

    /** 向量维度 */
    private Integer dimension;

    /** 每条输入文本对应的嵌入向量 */
    private List<float[]> embeddings;
}
