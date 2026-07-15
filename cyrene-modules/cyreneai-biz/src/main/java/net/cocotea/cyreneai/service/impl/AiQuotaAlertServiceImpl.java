package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertAddDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertPageDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertUpdateDTO;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.model.po.AiQuotaAlert;
import net.cocotea.cyreneai.model.vo.AiQuotaAlertVO;
import net.cocotea.cyreneai.service.AiQuotaAlertService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AiQuotaAlertServiceImpl implements AiQuotaAlertService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiQuotaAlertAddDTO param) {
        AiQuotaAlert alert = lightDao.convertType(param, AiQuotaAlert.class);
        if (alert.getEnableStatus() == null) alert.setEnableStatus(1);
        if (alert.getTriggerCount() == null) alert.setTriggerCount(0);
        if (alert.getNotifyChannel() == null) alert.setNotifyChannel("system");
        Object saved = lightDao.save(alert);
        return saved != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiQuotaAlertUpdateDTO param) {
        AiQuotaAlert alert = lightDao.convertType(param, AiQuotaAlert.class);
        // 触发状态字段不允许通过 update 覆盖
        alert.setLastTriggeredTime(null);
        alert.setTriggerCount(null);
        Long update = lightDao.update(alert);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiQuotaAlertVO> listByPage(AiQuotaAlertPageDTO pageDTO) {
        AiQuotaAlertPageDTO.Query query = pageDTO.getAiQuotaAlert();
        Map<String, Object> map = MapUtil.newHashMap(5);
        map.put("name", query != null ? query.getName() : null);
        map.put("scope", query != null ? query.getScope() : null);
        map.put("metric", query != null ? query.getMetric() : null);
        map.put("apiKeyId", query != null ? query.getApiKeyId() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiQuotaAlertVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_quota_alert_findList", map, AiQuotaAlertVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiQuotaAlert alert = new AiQuotaAlert().setId(id).setIsDeleted(1);
        Long update = lightDao.update(alert);
        return update != null && update > 0;
    }

    @Override
    public void evaluate(AiApiKey key) {
        try {
            Map<String, Object> map = MapUtil.newHashMap(1);
            map.put("apiKeyId", key != null ? key.getId() : null);
            List<AiQuotaAlert> rules = lightDao.find("ai_quota_alert_findEnabled", map, AiQuotaAlert.class);
            for (AiQuotaAlert rule : rules) {
                boolean triggered = false;
                String metric = rule.getMetric();
                if ("monthly_tokens".equals(metric) && key != null && key.getMonthlyTokenQuota() != null
                        && key.getTokensUsedThisMonth() != null && key.getMonthlyTokenQuota() > 0) {
                    long used = key.getTokensUsedThisMonth();
                    long quota = key.getMonthlyTokenQuota();
                    if (rule.getThresholdPercent() != null) {
                        double ratio = (double) used * 100.0 / quota;
                        if (ratio >= rule.getThresholdPercent()) triggered = true;
                    } else if (rule.getThresholdValue() != null) {
                        if (BigDecimal.valueOf(used).compareTo(rule.getThresholdValue()) >= 0) triggered = true;
                    }
                }
                if (triggered) {
                    AiQuotaAlert update = new AiQuotaAlert(rule.getId());
                    update.setLastTriggeredTime(LocalDateTime.now());
                    update.setTriggerCount((rule.getTriggerCount() == null ? 0 : rule.getTriggerCount()) + 1);
                    lightDao.update(update);
                    log.warn("[quota-alert] rule={} metric={} triggered for apiKeyId={}", rule.getName(), metric,
                            key != null ? key.getId() : null);
                    // TODO: 后续接入 email / webhook 通知
                }
            }
        } catch (Exception e) {
            log.error("evaluate quota alert failed", e);
        }
    }
}
