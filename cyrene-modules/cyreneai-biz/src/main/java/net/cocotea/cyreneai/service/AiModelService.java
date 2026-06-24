package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelVO;

public interface AiModelService extends BaseService<
        ApiPage<AiModelVO>,
        AiModelPageDTO,
        AiModelAddDTO,
        AiModelUpdateDTO
        > {
}
