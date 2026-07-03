package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneai.model.dto.AiImageModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.ImageGenerateDTO;
import net.cocotea.cyreneai.model.vo.AiImageModelVO;
import net.cocotea.cyreneai.model.vo.AiImageRecordVO;
import net.cocotea.cyreneai.service.rag.ImageService;
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
@Mapping("/ai/image")
public class ImageController {

    @Inject
    private ImageService imageService;

    @Post
    @Mapping("/generate")
    public ApiResult<String> generate(@Validated @Body ImageGenerateDTO param) {
        String url = imageService.generate(param);
        return ApiResult.ok(url);
    }

    @Post
    @Mapping("/model/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> addModel(@Validated @Body AiImageModelAddDTO param) {
        boolean b = imageService.add(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> updateModel(@Validated @Body AiImageModelUpdateDTO param) {
        boolean b = imageService.update(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteModel(@Validated @Body List<BigInteger> ids) {
        boolean b = imageService.deleteBatch(ids);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/listByPage")
    public ApiResult<ApiPage<AiImageModelVO>> listModelByPage(@Validated @Body AiImageModelPageDTO pageDTO) {
        ApiPage<AiImageModelVO> p = imageService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Post
    @Mapping("/record/listByPage")
    public ApiResult<ApiPage<AiImageRecordVO>> listRecordByPage(@Validated @Body AiImageModelPageDTO pageDTO) {
        ApiPage<AiImageRecordVO> p = imageService.listHistoryByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Post
    @Mapping("/record/delete/{id}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteRecord(BigInteger id) {
        boolean b = imageService.deleteHistory(id);
        return ApiResult.ok(b);
    }

    @Get
    @Mapping("/model/listEnabled")
    public ApiResult<List<AiImageModelVO>> listEnabled() {
        List<AiImageModelVO> list = imageService.listModels();
        return ApiResult.ok(list);
    }
}
