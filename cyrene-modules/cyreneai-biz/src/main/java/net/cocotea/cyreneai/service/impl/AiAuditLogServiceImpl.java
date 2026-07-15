package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiAuditLogPageDTO;
import net.cocotea.cyreneai.model.po.AiAuditLog;
import net.cocotea.cyreneai.model.vo.AiAuditLogVO;
import net.cocotea.cyreneai.service.AiAuditLogService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
public class AiAuditLogServiceImpl implements AiAuditLogService {

    @Db
    private LightDao lightDao;

    @Override
    public void record(AiAuditLog log) {
        try {
            if (log.getCreateTime() == null) log.setCreateTime(LocalDateTime.now());
            if (log.getStatus() == null) log.setStatus("unknown");
            if (log.getEndpoint() == null) log.setEndpoint("");
            lightDao.save(log);
        } catch (Exception e) {
            AiAuditLogServiceImpl.log.error("record audit log failed", e);
        }
    }

    @Override
    public ApiPage<AiAuditLogVO> listByPage(AiAuditLogPageDTO pageDTO) {
        AiAuditLogPageDTO.Query query = pageDTO.getAiAuditLog();
        Map<String, Object> map = MapUtil.newHashMap(7);
        map.put("userId", query != null ? query.getUserId() : null);
        map.put("apiKeyId", query != null ? query.getApiKeyId() : null);
        map.put("modelId", query != null ? query.getModelId() : null);
        map.put("endpoint", query != null ? query.getEndpoint() : null);
        map.put("status", query != null ? query.getStatus() : null);
        map.put("startTime", query != null ? query.getStartTime() : null);
        map.put("endTime", query != null ? query.getEndTime() : null);
        Page<AiAuditLogVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_audit_log_findList", map, AiAuditLogVO.class);
        return ApiPage.rest(page);
    }
}
