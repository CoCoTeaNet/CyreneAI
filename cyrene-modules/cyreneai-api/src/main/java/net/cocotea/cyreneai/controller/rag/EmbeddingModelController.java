package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiEmbeddingModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiEmbeddingModel;
import net.cocotea.cyreneai.model.vo.AiEmbeddingModelVO;
import net.cocotea.cyreneai.service.rag.EmbeddingService;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.annotation.Get;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Valid
@Controller
@Mapping("/ai/embedding/model")
public class EmbeddingModelController {

    @Inject
    private EmbeddingService embeddingService;

    @Post
    @Mapping("/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Validated @Body AiEmbeddingModelAddDTO param) {
        AiEmbeddingModel model = BeanUtil.copyProperties(param, AiEmbeddingModel.class);
        boolean b = embeddingService.add(model);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiEmbeddingModelUpdateDTO param) {
        AiEmbeddingModel model = BeanUtil.copyProperties(param, AiEmbeddingModel.class);
        boolean b = embeddingService.update(model);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/delete/{id}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> delete(BigInteger id) throws BusinessException {
        boolean b = embeddingService.delete(id);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<AiEmbeddingModelVO>> listByPage(@Validated @Body AiEmbeddingModelPageDTO pageDTO) {
        AiEmbeddingModelPageDTO.Query q = pageDTO.getEmbeddingModel();
        AiEmbeddingModel query = new AiEmbeddingModel();
        if (q != null) {
            query.setProviderType(q.getProviderType());
            query.setModelName(q.getModelName());
            query.setEnableStatus(q.getEnableStatus());
        }
        ApiPage<AiEmbeddingModelVO> p = embeddingService.listByPage(query,
                pageDTO.getPageNo(), pageDTO.getPageSize());
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/listEnabled")
    public ApiResult<List<AiEmbeddingModelVO>> listEnabled() {
        List<AiEmbeddingModelVO> list = embeddingService.listEnabled();
        return ApiResult.ok(list);
    }
}
