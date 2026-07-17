package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetAddDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetPageDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiEvalDatasetVO;
import net.cocotea.cyreneai.service.AiEvalDatasetService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/evalDataset")
@Valid
public class AiEvalDatasetController {

    @Inject
    private AiEvalDatasetService aiEvalDatasetService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiEvalDatasetAddDTO param) {
        boolean b = aiEvalDatasetService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        boolean b = aiEvalDatasetService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiEvalDatasetUpdateDTO param) {
        boolean b = aiEvalDatasetService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiEvalDatasetVO>> listByPage(@Validated @Body AiEvalDatasetPageDTO pageDTO) {
        ApiPage<AiEvalDatasetVO> p = aiEvalDatasetService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
