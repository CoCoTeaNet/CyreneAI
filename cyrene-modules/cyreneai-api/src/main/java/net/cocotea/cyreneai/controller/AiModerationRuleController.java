package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiModerationRuleAddDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRulePageDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRuleUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModerationRuleVO;
import net.cocotea.cyreneai.service.AiModerationRuleService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/moderationRule")
@Valid
public class AiModerationRuleController {

    @Inject
    private AiModerationRuleService aiModerationRuleService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiModerationRuleAddDTO param) {
        boolean b = aiModerationRuleService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiModerationRuleService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiModerationRuleUpdateDTO param) {
        boolean b = aiModerationRuleService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiModerationRuleVO>> listByPage(@Validated @Body AiModerationRulePageDTO pageDTO) {
        ApiPage<AiModerationRuleVO> p = aiModerationRuleService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
