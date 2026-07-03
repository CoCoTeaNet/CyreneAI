package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneai.model.dto.AiVisionModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiVisionModelVO;
import net.cocotea.cyreneai.service.rag.VisionService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Valid
@Controller
@Mapping("/ai/vision-model")
public class VisionController {

    @Inject
    private VisionService visionService;

    @Post
    @Mapping("/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiVisionModelAddDTO param) {
        boolean b = visionService.add(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiVisionModelUpdateDTO param) {
        boolean b = visionService.update(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> ids) {
        boolean b = visionService.deleteBatch(ids);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<AiVisionModelVO>> listByPage(@Validated @Body AiVisionModelPageDTO pageDTO) {
        ApiPage<AiVisionModelVO> p = visionService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/listEnabled")
    public ApiResult<List<AiVisionModelVO>> listEnabled() {
        List<AiVisionModelVO> list = visionService.listEnabledModels();
        return ApiResult.ok(list);
    }
}
