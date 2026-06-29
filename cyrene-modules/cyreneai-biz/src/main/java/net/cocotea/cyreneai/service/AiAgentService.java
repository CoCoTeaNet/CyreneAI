package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiAgentAddDTO;
import net.cocotea.cyreneai.model.dto.AiAgentPageDTO;
import net.cocotea.cyreneai.model.dto.AiAgentUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiAgentVO;

import java.util.List;

public interface AiAgentService extends BaseService<
        ApiPage<AiAgentVO>,
        AiAgentPageDTO,
        AiAgentAddDTO,
        AiAgentUpdateDTO
        > {

    List<AiAgentVO> listEnabled();
}
