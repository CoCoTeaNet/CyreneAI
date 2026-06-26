package net.cocotea.cyreneai.controller.rag;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.bean.BeanUtil;
import net.cocotea.cyreneai.model.dto.AiDocumentPageDTO;
import net.cocotea.cyreneai.model.dto.AiDocumentUpdateDTO;
import net.cocotea.cyreneai.model.po.AiDocument;
import net.cocotea.cyreneai.model.vo.AiDocumentVO;
import net.cocotea.cyreneai.service.rag.DocumentService;
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

@Valid
@Controller
@Mapping("/ai/document")
public class DocumentController {

    @Inject
    private DocumentService documentService;

    @Post
    @Mapping("/upload")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<AiDocument> upload(@Param("file") UploadedFile file,
                                         @Param(value = "kbId", required = false) BigInteger kbId,
                                         @Param("chunkStrategy") String chunkStrategy,
                                         @Param("chunkSize") Integer chunkSize,
                                         @Param("chunkOverlap") Integer chunkOverlap) throws IOException {
        if (file == null) {
            throw new RuntimeException("缺少上传文件");
        }
        byte[] content;
        try (InputStream in = file.getContent()) {
            content = in.readAllBytes();
        }
        AiDocument doc = documentService.upload(
                file.getName(), content, kbId,
                chunkStrategy, chunkSize, chunkOverlap);
        return ApiResult.ok(doc);
    }

    @Post
    @Mapping("/reIndex/{id}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> reIndex(@Param("id") BigInteger id) {
        documentService.reIndex(id);
        return ApiResult.ok();
    }

    @Post
    @Mapping("/delete/{id}")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<?> delete(@Param("id") BigInteger id) throws BusinessException {
        documentService.delete(id);
        return ApiResult.ok();
    }

    @Post
    @Mapping("/listByPage")
    public ApiResult<ApiPage<AiDocumentVO>> listByPage(@Validated @Body AiDocumentPageDTO pageDTO) {
        AiDocumentPageDTO.Query q = pageDTO.getDocument();
        AiDocument query = new AiDocument();
        if (q != null) {
            query.setName(q.getName());
            query.setType(q.getType());
            query.setStatus(q.getStatus());
            query.setKbId(q.getKbId());
        }
        ApiPage<AiDocumentVO> p = documentService.listByPage(query,
                pageDTO.getPageNo(), pageDTO.getPageSize());
        return ApiResult.ok(p);
    }

    @Get
    @Mapping("/get/{id}")
    public ApiResult<AiDocument> get(@Param("id") BigInteger id) {
        AiDocument doc = documentService.getById(id);
        return ApiResult.ok(doc);
    }
}
