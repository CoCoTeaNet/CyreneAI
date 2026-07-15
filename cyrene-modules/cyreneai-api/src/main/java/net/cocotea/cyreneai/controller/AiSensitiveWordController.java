package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordAddDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordPageDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiSensitiveWordVO;
import net.cocotea.cyreneai.service.AiSensitiveWordService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/sensitiveWord")
@Valid
public class AiSensitiveWordController {

    @Inject
    private AiSensitiveWordService aiSensitiveWordService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiSensitiveWordAddDTO param) {
        boolean b = aiSensitiveWordService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiSensitiveWordService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiSensitiveWordUpdateDTO param) {
        boolean b = aiSensitiveWordService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiSensitiveWordVO>> listByPage(@Validated @Body AiSensitiveWordPageDTO pageDTO) {
        ApiPage<AiSensitiveWordVO> p = aiSensitiveWordService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
