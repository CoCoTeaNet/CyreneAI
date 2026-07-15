package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiPromptPresetAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptPresetVO;
import net.cocotea.cyreneai.service.AiPromptPresetService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/promptPreset")
@Valid
public class AiPromptPresetController {

    @Inject
    private AiPromptPresetService aiPromptPresetService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiPromptPresetAddDTO param) {
        return ApiResult.ok(aiPromptPresetService.add(param));
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        return ApiResult.ok(aiPromptPresetService.deleteBatch(list));
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiPromptPresetUpdateDTO param) {
        return ApiResult.ok(aiPromptPresetService.update(param));
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiPromptPresetVO>> listByPage(@Validated @Body AiPromptPresetPageDTO pageDTO) {
        return ApiResult.ok(aiPromptPresetService.listByPage(pageDTO));
    }

    @Mapping("/listEnabled")
    @Get
    public ApiResult<List<AiPromptPresetVO>> listEnabled(@Param("category") String category) {
        return ApiResult.ok(aiPromptPresetService.listEnabled(category));
    }
}
