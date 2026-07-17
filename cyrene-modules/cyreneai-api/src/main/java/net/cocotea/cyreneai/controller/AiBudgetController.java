package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiBudgetAddDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetPageDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiBudgetStatusVO;
import net.cocotea.cyreneai.model.vo.AiBudgetVO;
import net.cocotea.cyreneai.service.AiBudgetService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/budget")
@Valid
public class AiBudgetController {

    @Inject
    private AiBudgetService aiBudgetService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiBudgetAddDTO param) {
        boolean b = aiBudgetService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiBudgetService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiBudgetUpdateDTO param) {
        boolean b = aiBudgetService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiBudgetVO>> listByPage(@Validated @Body AiBudgetPageDTO pageDTO) {
        ApiPage<AiBudgetVO> p = aiBudgetService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Mapping("/status")
    @Get
    public ApiResult<List<AiBudgetStatusVO>> listStatus() {
        List<AiBudgetStatusVO> list = aiBudgetService.listStatus();
        return ApiResult.ok(list);
    }
}
