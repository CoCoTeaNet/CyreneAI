package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiModerationRuleAddDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRulePageDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRuleUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModerationRule;
import net.cocotea.cyreneai.model.vo.AiModerationRuleVO;

import java.util.List;

public interface AiModerationRuleService extends BaseService<
        ApiPage<AiModerationRuleVO>,
        AiModerationRulePageDTO,
        AiModerationRuleAddDTO,
        AiModerationRuleUpdateDTO
        > {

    List<AiModerationRule> listEnabled();
}
