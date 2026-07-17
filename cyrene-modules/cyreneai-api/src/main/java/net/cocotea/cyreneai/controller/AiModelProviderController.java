package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;
import net.cocotea.cyreneai.service.AiModelProviderService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Controller
@Mapping("/ai/model-provider")
@Valid
public class AiModelProviderController {

    @Inject
    private AiModelProviderService aiModelProviderService;

    @Mapping("/add")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiModelProviderAddDTO param) throws BusinessException {
        boolean b = aiModelProviderService.add(param);
        return ApiResult.ok(b);
    }

    @Mapping("/deleteBatch")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Validated @Body List<BigInteger> list) throws BusinessException {
        boolean b = aiModelProviderService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    @Mapping("/update")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiModelProviderUpdateDTO param) throws BusinessException {
        boolean b = aiModelProviderService.update(param);
        return ApiResult.ok(b);
    }

    @Mapping("/listByPage")
    @Post
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<ApiPage<AiModelProviderVO>> listByPage(@Validated @Body AiModelProviderPageDTO pageDTO) throws BusinessException {
        ApiPage<AiModelProviderVO> p = aiModelProviderService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }

    /**
     * 密钥轮换：用旧主密钥解密、新主密钥重新加密所有提供商密钥。
     * 轮换后需将配置 {@code myapp.ai.api-key-secret} 更新为新密钥并重启。
     */
    @Mapping("/rotate-secret/{oldSecret}/{newSecret}")
    @Post
    @SaCheckRole(value = {"role:super:admin"}, mode = SaMode.OR)
    public ApiResult<Integer> rotateSecret(@Param("oldSecret") String oldSecret,
                                           @Param("newSecret") String newSecret) throws BusinessException {
        int count = aiModelProviderService.rotateSecret(oldSecret, newSecret);
        return ApiResult.ok(count);
    }
}
