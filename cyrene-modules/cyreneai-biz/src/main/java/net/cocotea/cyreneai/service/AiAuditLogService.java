package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiAuditLogPageDTO;
import net.cocotea.cyreneai.model.po.AiAuditLog;
import net.cocotea.cyreneai.model.vo.AiAuditLogVO;

public interface AiAuditLogService {

    /**
     * 异步落库(内部 catch 全部异常, 不影响主链路)
     */
    void record(AiAuditLog log);

    ApiPage<AiAuditLogVO> listByPage(AiAuditLogPageDTO pageDTO);
}
