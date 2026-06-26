package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import net.cocotea.cyreneai.model.dto.AiKnowledgeBaseAddDTO;
import net.cocotea.cyreneai.model.dto.AiKnowledgeBasePageDTO;
import net.cocotea.cyreneai.model.dto.AiKnowledgeBaseQueryDTO;
import net.cocotea.cyreneai.model.dto.AiKnowledgeBaseUpdateDTO;
import net.cocotea.cyreneai.model.po.AiKnowledgeBase;
import net.cocotea.cyreneai.model.vo.AiKnowledgeBaseVO;
import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;
import net.cocotea.cyreneai.service.rag.KnowledgeBaseService;
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
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

import java.math.BigInteger;
import java.util.List;

@Valid
@Controller
@Mapping("/ai/knowledgeBase")
public class KnowledgeBaseController {

    @Inject
    private KnowledgeBaseService knowledgeBaseService;

    @Post
    @Mapping("/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<AiKnowledgeBase> add(@Validated @Body AiKnowledgeBaseAddDTO param) {
        AiKnowledgeBase kb = BeanUtil.copyProperties(param, AiKnowledgeBase.class);
        AiKnowledgeBase created = knowledgeBaseService.add(kb);
        return ApiResult.ok(created);
    }

    @Post
    @Mapping("/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Validated @Body AiKnowledgeBaseUpdateDTO param) {
        AiKnowledgeBase kb = BeanUtil.copyProperties(param, AiKnowledgeBase.class);
        boolean b = knowledgeBaseService.update(kb);
        return ApiResult.ok(b);
    }

    @Post
    @Mapping("/delete/{id}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> delete(@Param("id") BigInteger id) throws BusinessException {
        boolean b = knowledgeBaseService.delete(id);
        return ApiResult.ok(b);
    }

    @Get
    @Mapping("/get/{id}")
    public ApiResult<AiKnowledgeBase> get(@Param("id") BigInteger id) {
        AiKnowledgeBase kb = knowledgeBaseService.getById(id);
        return ApiResult.ok(kb);
    }

    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<AiKnowledgeBaseVO>> listByPage(@Validated @Body AiKnowledgeBasePageDTO pageDTO) {
        AiKnowledgeBasePageDTO.Query q = pageDTO.getKnowledgeBase();
        AiKnowledgeBase query = new AiKnowledgeBase();
        if (q != null) {
            query.setName(q.getName());
            query.setEnableStatus(q.getEnableStatus());
        }
        ApiPage<AiKnowledgeBaseVO> p = knowledgeBaseService.listByPage(query,
                pageDTO.getPageNo(), pageDTO.getPageSize());
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/listEnabled")
    public ApiResult<List<AiKnowledgeBaseVO>> listEnabled() {
        List<AiKnowledgeBaseVO> list = knowledgeBaseService.listEnabled();
        return ApiResult.ok(list);
    }

    @Post
    @Mapping("/retrieve")
    public ApiResult<List<AiRetrievalResultVO>> retrieve(@Validated @Body AiKnowledgeBaseQueryDTO query) {
        List<AiRetrievalResultVO> results = knowledgeBaseService.retrieve(
                query.getKbId(), query.getQuery(), query.getTopK(),
                query.getSimilarityThreshold(), query.getRetrievalStrategy());
        return ApiResult.ok(results);
    }

    @Post
    @Mapping("/addDocument/{kbId}/{documentId}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> addDocument(@Param("kbId") BigInteger kbId, @Param("documentId") BigInteger documentId) {
        knowledgeBaseService.addDocument(kbId, documentId);
        return ApiResult.ok();
    }

    @Post
    @Mapping("/removeDocument/{kbId}/{documentId}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> removeDocument(@Param("kbId") BigInteger kbId, @Param("documentId") BigInteger documentId) {
        knowledgeBaseService.removeDocument(kbId, documentId);
        return ApiResult.ok();
    }
}
