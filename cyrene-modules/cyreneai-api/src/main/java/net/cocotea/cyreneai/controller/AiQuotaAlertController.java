package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertAddDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertPageDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiQuotaAlertVO;
import net.cocotea.cyreneai.service.AiQuotaAlertService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/quotaAlert")
@Valid
public class AiQuotaAlertController {

    @Inject
    private AiQuotaAlertService aiQuotaAlertService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiQuotaAlertAddDTO param) {
        boolean b = aiQuotaAlertService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiQuotaAlertService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiQuotaAlertUpdateDTO param) {
        boolean b = aiQuotaAlertService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiQuotaAlertVO>> listByPage(@Validated @Body AiQuotaAlertPageDTO pageDTO) {
        ApiPage<AiQuotaAlertVO> p = aiQuotaAlertService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
