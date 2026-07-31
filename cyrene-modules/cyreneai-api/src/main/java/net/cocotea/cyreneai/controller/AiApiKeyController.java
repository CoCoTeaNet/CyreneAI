package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiApiKeyAddDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyPageDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiApiKeyUsageVO;
import net.cocotea.cyreneai.model.vo.AiApiKeyVO;
import net.cocotea.cyreneai.service.AiApiKeyService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/apiKey")
@Valid
public class AiApiKeyController {

    @Inject
    private AiApiKeyService aiApiKeyService;

    @Mapping("/generate")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<AiApiKeyVO> generate(@Validated @Body AiApiKeyAddDTO param) {
        AiApiKeyVO vo = aiApiKeyService.generate(param);
        return ApiResult.ok(vo);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiApiKeyService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiApiKeyUpdateDTO param) {
        boolean b = aiApiKeyService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<ApiPage<AiApiKeyVO>> listByPage(@Validated @Body AiApiKeyPageDTO pageDTO) {
        ApiPage<AiApiKeyVO> p = aiApiKeyService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Mapping("/usage/{apiKeyId}")
    @Get
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<List<AiApiKeyUsageVO>> statRecent(@Param("apiKeyId") BigInteger apiKeyId,
                                                       @Param(value = "days", required = false) Integer days) {
        List<AiApiKeyUsageVO> list = aiApiKeyService.statRecent(apiKeyId, days);
        return ApiResult.ok(list);
    }
}
