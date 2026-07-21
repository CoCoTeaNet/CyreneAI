package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * 文本嵌入请求参数。
 * <p>
 * {@code modelId} 为空时使用默认嵌入模型；{@code input} 为待嵌入的文本列表。
 */
@Data
@Accessors(chain = true)
public class AiEmbeddingRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 嵌入模型ID，为空时使用默认嵌入模型 */
    private BigInteger modelId;

    /** 待嵌入的文本列表 */
    private List<String> input;
}
