package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiBudgetAddDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetPageDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiBudgetStatusVO;
import net.cocotea.cyreneai.model.vo.AiBudgetVO;

import java.util.List;

public interface AiBudgetService extends BaseService<
        ApiPage<AiBudgetVO>,
        AiBudgetPageDTO,
        AiBudgetAddDTO,
        AiBudgetUpdateDTO
        > {

    /**
     * 实时计算所有启用中预算的执行状态(当前周期已用花费 / 占比 / 告警)
     */
    List<AiBudgetStatusVO> listStatus();
}
