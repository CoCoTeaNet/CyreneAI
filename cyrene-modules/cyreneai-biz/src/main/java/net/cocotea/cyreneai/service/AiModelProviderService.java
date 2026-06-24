package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;

public interface AiModelProviderService extends BaseService<
        ApiPage<AiModelProviderVO>,
        AiModelProviderPageDTO,
        AiModelProviderAddDTO,
        AiModelProviderUpdateDTO
        > {
}
