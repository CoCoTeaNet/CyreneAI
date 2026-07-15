package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptAbTestVO;
import net.cocotea.cyreneai.service.AiPromptAbTestService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/promptAbTest")
@Valid
public class AiPromptAbTestController {

    @Inject
    private AiPromptAbTestService aiPromptAbTestService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiPromptAbTestAddDTO param) {
        return ApiResult.ok(aiPromptAbTestService.add(param));
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        return ApiResult.ok(aiPromptAbTestService.deleteBatch(list));
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiPromptAbTestUpdateDTO param) {
        return ApiResult.ok(aiPromptAbTestService.update(param));
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiPromptAbTestVO>> listByPage(@Validated @Body AiPromptAbTestPageDTO pageDTO) {
        return ApiResult.ok(aiPromptAbTestService.listByPage(pageDTO));
    }

    @Mapping("/detail/{id}")
    @Get
    public ApiResult<AiPromptAbTestVO> detail(@Param("id") BigInteger id) {
        return ApiResult.ok(aiPromptAbTestService.detail(id));
    }

    @Mapping("/changeStatus/{id}/{status}")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> changeStatus(@Param("id") BigInteger id, @Param("status") String status) {
        return ApiResult.ok(aiPromptAbTestService.changeStatus(id, status));
    }
}
