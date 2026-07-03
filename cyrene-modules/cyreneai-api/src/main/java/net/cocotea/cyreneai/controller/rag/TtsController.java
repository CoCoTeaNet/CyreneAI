package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneai.model.dto.AiTtsModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.TtsSynthesizeDTO;
import net.cocotea.cyreneai.model.vo.AiTtsModelVO;
import net.cocotea.cyreneai.model.vo.AiTtsRecordVO;
import net.cocotea.cyreneai.service.rag.TtsService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiPageDTO;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.annotation.Get;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Valid
@Controller
@Mapping("/ai/tts")
public class TtsController {

    @Inject
    private TtsService ttsService;

    @Post
    @Mapping("/synthesize")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public void synthesize(@Validated @Body TtsSynthesizeDTO dto, Context ctx) {
        byte[] audioBytes = ttsService.synthesize(dto);
        ctx.contentType("audio/mpeg");
        ctx.output(audioBytes);
    }

    @Post
    @Mapping("/synthesize-url")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Map<String, String>> synthesizeUrl(@Validated @Body TtsSynthesizeDTO dto) {
        byte[] audioBytes = ttsService.synthesize(dto);
        Map<String, String> result = new HashMap<>();
        result.put("audioSize", String.valueOf(audioBytes.length));
        return ApiResult.ok(result);
    }

    @Post
    @Mapping("/model/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> addModel(@Validated @Body AiTtsModelAddDTO param) {
        boolean b = ttsService.add(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> updateModel(@Validated @Body AiTtsModelUpdateDTO param) {
        boolean b = ttsService.update(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = ttsService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/listByPage")
    public ApiResult<ApiPage<AiTtsModelVO>> listByPage(@Validated @Body AiTtsModelPageDTO pageDTO) {
        ApiPage<AiTtsModelVO> p = ttsService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/model/listEnabled")
    public ApiResult<List<AiTtsModelVO>> listEnabled() {
        List<AiTtsModelVO> list = ttsService.listModels();
        return ApiResult.ok(list);
    }

    @Post
    @Mapping("/record/listByPage")
    public ApiResult<ApiPage<AiTtsRecordVO>> listRecordByPage(@Validated @Body ApiPageDTO pageDTO) {
        ApiPage<AiTtsRecordVO> p = ttsService.listHistoryByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
