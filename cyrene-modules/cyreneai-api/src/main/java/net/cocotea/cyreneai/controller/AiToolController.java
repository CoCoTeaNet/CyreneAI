package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.agent.ToolExecutionService;
import net.cocotea.cyreneai.model.dto.AiToolAddDTO;
import net.cocotea.cyreneai.model.dto.AiToolPageDTO;
import net.cocotea.cyreneai.model.dto.AiToolUpdateDTO;
import net.cocotea.cyreneai.model.dto.ToolExecuteDTO;
import net.cocotea.cyreneai.model.vo.AiToolVO;
import net.cocotea.cyreneai.service.AiToolService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/tool")
@Valid
public class AiToolController {

    @Inject
    private AiToolService aiToolService;

    @Inject
    private ToolExecutionService toolExecutionService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiToolAddDTO param) {
        boolean b = aiToolService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiToolService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiToolUpdateDTO param) {
        boolean b = aiToolService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiToolVO>> listByPage(@Validated @Body AiToolPageDTO pageDTO) {
        ApiPage<AiToolVO> p = aiToolService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Mapping("/listEnabled")
    @Get
    public ApiResult<List<AiToolVO>> listEnabled() {
        List<AiToolVO> list = aiToolService.listEnabled();
        return ApiResult.ok(list);
    }

    @Mapping("/listByType/{type}")
    @Get
    public ApiResult<List<AiToolVO>> listByType(@Param("type") String type) {
        List<AiToolVO> list = aiToolService.listByType(type);
        return ApiResult.ok(list);
    }

    @Mapping("/execute")
    @Post
    public ApiResult<String> execute(@Validated @Body ToolExecuteDTO dto) {
        String result = toolExecutionService.executeBuiltin(dto.getToolName(), dto.getArguments());
        return ApiResult.ok(result);
    }

    @Mapping("/specifications")
    @Get
    public ApiResult<?> getSpecifications() {
        var specs = toolExecutionService.getAllSpecifications();
        return ApiResult.ok(specs);
    }
}
