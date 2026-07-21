package net.cocotea.cyreneai.controller.rag;

import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneai.model.dto.AiEmbeddingRequestDTO;
import net.cocotea.cyreneai.model.vo.AiEmbeddingResultVO;
import net.cocotea.cyreneai.service.rag.EmbeddingService;
import net.cocotea.cyreneadmin.model.ApiResult;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

/**
 * 文本嵌入接口。对外提供 {@code POST /ai/embeddings}，
 * 将输入文本转换为向量，可指定嵌入模型或使用默认嵌入模型。
 */
@Valid
@Controller
@Mapping("/ai")
public class EmbeddingController {

    @Inject
    private EmbeddingService embeddingService;

    @Post
    @Mapping("/embeddings")
    public ApiResult<AiEmbeddingResultVO> embeddings(@Validated @Body AiEmbeddingRequestDTO param) {
        if (param.getInput() == null || param.getInput().isEmpty()) {
            throw new BusinessException("input 不能为空");
        }
        AiEmbeddingResultVO result = embeddingService.embedTexts(param.getModelId(), param.getInput());
        return ApiResult.ok(result);
    }
}
