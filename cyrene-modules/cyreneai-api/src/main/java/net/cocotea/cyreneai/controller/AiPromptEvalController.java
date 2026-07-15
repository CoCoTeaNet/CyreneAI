package net.cocotea.cyreneai.controller;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiPromptEvalPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRateDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRunDTO;
import net.cocotea.cyreneai.model.vo.AiPromptEvalVO;
import net.cocotea.cyreneai.service.AiPromptEvalService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

@Controller
@Mapping("/ai/promptEval")
@Valid
public class AiPromptEvalController {

    @Inject
    private AiPromptEvalService aiPromptEvalService;

    @Mapping("/run")
    @Post
    public ApiResult<AiPromptEvalVO> run(@Validated @Body AiPromptEvalRunDTO dto) {
        return ApiResult.ok(aiPromptEvalService.run(dto));
    }

    @Mapping("/rate")
    @Post
    public ApiResult<Boolean> rate(@Validated @Body AiPromptEvalRateDTO dto) {
        return ApiResult.ok(aiPromptEvalService.rate(dto));
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiPromptEvalVO>> listByPage(@Validated @Body AiPromptEvalPageDTO pageDTO) {
        return ApiResult.ok(aiPromptEvalService.listByPage(pageDTO));
    }
}
