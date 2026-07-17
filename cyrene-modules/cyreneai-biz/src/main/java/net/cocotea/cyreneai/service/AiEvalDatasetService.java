package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetAddDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetPageDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiEvalDatasetVO;

public interface AiEvalDatasetService extends BaseService<
        ApiPage<AiEvalDatasetVO>,
        AiEvalDatasetPageDTO,
        AiEvalDatasetAddDTO,
        AiEvalDatasetUpdateDTO
        > {
}
