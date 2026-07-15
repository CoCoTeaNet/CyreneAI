package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertAddDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertPageDTO;
import net.cocotea.cyreneai.model.dto.AiQuotaAlertUpdateDTO;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.model.vo.AiQuotaAlertVO;

public interface AiQuotaAlertService extends BaseService<
        ApiPage<AiQuotaAlertVO>,
        AiQuotaAlertPageDTO,
        AiQuotaAlertAddDTO,
        AiQuotaAlertUpdateDTO
        > {

    /**
     * 评估当前用量对已配置告警的影响; 命中阈值则更新 last_triggered_time 与 trigger_count
     * @param key 触发用量的 API Key(可为空表示全局评估)
     */
    void evaluate(AiApiKey key);
}
