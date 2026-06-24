package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;
import net.cocotea.cyreneai.service.AiModelProviderService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/model-provider")
@Valid
public class AiModelProviderController {

    @Inject
    private AiModelProviderService aiModelProviderService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiModelProviderAddDTO param) throws BusinessException {
        boolean b = aiModelProviderService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = aiModelProviderService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiModelProviderUpdateDTO param) throws BusinessException {
        boolean b = aiModelProviderService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<ApiPage<AiModelProviderVO>> listByPage(@Validated @Body AiModelProviderPageDTO pageDTO) throws BusinessException {
        ApiPage<AiModelProviderVO> p = aiModelProviderService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
