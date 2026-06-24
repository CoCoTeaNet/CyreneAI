package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneai.model.dto.AiModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelVO;
import net.cocotea.cyreneai.service.AiModelService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Mapping("/ai/model")
@Valid
public class AiModelController {

    @Inject
    private AiModelService aiModelService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiModelAddDTO param) throws BusinessException {
        boolean b = aiModelService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = aiModelService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiModelUpdateDTO param) throws BusinessException {
        boolean b = aiModelService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<ApiPage<AiModelVO>> listByPage(@Validated @Body AiModelPageDTO pageDTO) throws BusinessException {
        ApiPage<AiModelVO> p = aiModelService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Mapping("/listEnabled")
    @Get
    public ApiResult<List<AiModelVO>> listEnabled() {
        List<AiModelVO> list = aiModelService.listEnabled();
        return ApiResult.ok(list);
    }
}
