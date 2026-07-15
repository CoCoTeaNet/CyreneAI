package net.cocotea.cyreneai.controller;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneai.model.dto.AiAuditLogPageDTO;
import net.cocotea.cyreneai.model.vo.AiAuditLogVO;
import net.cocotea.cyreneai.service.AiAuditLogService;
import org.noear.solon.annotation.*;
import org.noear.solon.validation.annotation.Valid;
import org.noear.solon.validation.annotation.Validated;

@Controller
@Mapping("/ai/auditLog")
@Valid
public class AiAuditLogController {

    @Inject
    private AiAuditLogService aiAuditLogService;

    @Mapping("/listByPage")
    @Post
    public ApiResult<ApiPage<AiAuditLogVO>> listByPage(@Validated @Body AiAuditLogPageDTO pageDTO) {
        ApiPage<AiAuditLogVO> p = aiAuditLogService.listByPage(pageDTO);
        return ApiResult.ok(p);
    }
}
