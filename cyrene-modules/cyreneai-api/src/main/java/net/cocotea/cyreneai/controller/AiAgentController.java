package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiAgentAddDTO;
import net.cocotea.cyreneai.model.dto.AiAgentPageDTO;
import net.cocotea.cyreneai.model.dto.AiAgentUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiAgentVO;
import net.cocotea.cyreneai.service.AiAgentService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/agent")
@Valid
public class AiAgentController {

    @Inject
    private AiAgentService aiAgentService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiAgentAddDTO param) {
        boolean b = aiAgentService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiAgentService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiAgentUpdateDTO param) {
        boolean b = aiAgentService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiAgentVO>> listByPage(@Validated @Body AiAgentPageDTO pageDTO) {
        ApiPage<AiAgentVO> p = aiAgentService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Mapping("/listEnabled")
    @Get
    public ApiResult<List<AiAgentVO>> listEnabled() {
        List<AiAgentVO> list = aiAgentService.listEnabled();
        return ApiResult.ok(list);
    }
}
