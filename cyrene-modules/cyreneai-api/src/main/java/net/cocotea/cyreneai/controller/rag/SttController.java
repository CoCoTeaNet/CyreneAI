package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneai.model.dto.AiSttModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.SttTranscribeDTO;
import net.cocotea.cyreneai.model.vo.AiSttModelVO;
import net.cocotea.cyreneai.model.vo.AiSttRecordVO;
import net.cocotea.cyreneai.service.rag.SttService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Param;
import org.noear.solon.core.handle.UploadedFile;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Valid
@Controller
@Mapping("/ai/stt")
public class SttController {

    @Inject
    private SttService sttService;

    @Post
    @Mapping("/transcribe")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Map<String, String>> transcribe(@Param("file") UploadedFile file,
                                                       @Param(value = "modelId", required = false) BigInteger modelId) throws IOException {
        if (file == null) {
            throw new RuntimeException("缺少上传音频文件");
        }
        String transcript;
        try (InputStream in = file.getContent()) {
            transcript = sttService.transcribeFile(in, file.getName(), modelId);
        }
        Map<String, String> result = new HashMap<>();
        result.put("text", transcript);
        return ApiResult.ok(result);
    }

    @Post
    @Mapping("/transcribe-url")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Map<String, String>> transcribeUrl(@Validated @Body SttTranscribeDTO dto) {
        String transcript = sttService.transcribe(dto);
        Map<String, String> result = new HashMap<>();
        result.put("text", transcript);
        return ApiResult.ok(result);
    }

    @Post
    @Mapping("/model/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> addModel(@Validated @Body AiSttModelAddDTO param) {
        boolean b = sttService.add(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> updateModel(@Validated @Body AiSttModelUpdateDTO param) {
        boolean b = sttService.update(param);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = sttService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/model/listByPage")
    public ApiResult<ApiPage<AiSttModelVO>> listByPage(@Validated @Body AiSttModelPageDTO pageDTO) {
        ApiPage<AiSttModelVO> p = sttService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/model/listEnabled")
    public ApiResult<List<AiSttModelVO>> listEnabled() {
        List<AiSttModelVO> list = sttService.listEnabled();
        return ApiResult.ok(list);
    }

    @Post
    @Mapping("/record/listByPage")
    public ApiResult<ApiPage<AiSttRecordVO>> listRecordByPage(@Validated @Body AiSttModelPageDTO pageDTO) {
        ApiPage<AiSttRecordVO> p = sttService.listHistoryByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
