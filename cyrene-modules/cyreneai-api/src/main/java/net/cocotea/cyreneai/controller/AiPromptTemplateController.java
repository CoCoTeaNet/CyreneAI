package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiPromptRenderDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplatePageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVO;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVersionVO;
import net.cocotea.cyreneai.service.AiPromptTemplateService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/promptTemplate")
@Valid
public class AiPromptTemplateController {

    @Inject
    private AiPromptTemplateService aiPromptTemplateService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiPromptTemplateAddDTO param) {
        return ApiResult.ok(aiPromptTemplateService.add(param));
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) {
        return ApiResult.ok(aiPromptTemplateService.deleteBatch(list));
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiPromptTemplateUpdateDTO param) {
        return ApiResult.ok(aiPromptTemplateService.update(param));
    }

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiPromptTemplateVO>> listByPage(@Validated @Body AiPromptTemplatePageDTO pageDTO) {
        return ApiResult.ok(aiPromptTemplateService.listByPage(pageDTO));
    }

    @Mapping("/listEnabled")
    @Get
    public ApiResult<List<AiPromptTemplateVO>> listEnabled(@Param("category") String category,
                                                           @Param("scene") String scene) {
        return ApiResult.ok(aiPromptTemplateService.listEnabled(category, scene));
    }

    @Mapping("/render")
    @Post
    public ApiResult<String> render(@Validated @Body AiPromptRenderDTO dto) {
        return ApiResult.ok(aiPromptTemplateService.render(dto));
    }

    @Mapping("/listVersions/{templateId}")
    @Get
    public ApiResult<List<AiPromptTemplateVersionVO>> listVersions(@Param("templateId") BigInteger templateId) {
        return ApiResult.ok(aiPromptTemplateService.listVersions(templateId));
    }

    @Mapping("/getVersion/{templateId}/{version}")
    @Get
    public ApiResult<AiPromptTemplateVersionVO> getVersion(@Param("templateId") BigInteger templateId,
                                                           @Param("version") Integer version) {
        return ApiResult.ok(aiPromptTemplateService.getVersion(templateId, version));
    }

    @Mapping("/rollback/{templateId}/{version}")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> rollback(@Param("templateId") BigInteger templateId,
                                       @Param("version") Integer version,
                                       @Param("changeNote") String changeNote) {
        return ApiResult.ok(aiPromptTemplateService.rollback(templateId, version, changeNote));
    }
}
