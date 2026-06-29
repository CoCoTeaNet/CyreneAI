package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiToolAddDTO;
import net.cocotea.cyreneai.model.dto.AiToolPageDTO;
import net.cocotea.cyreneai.model.dto.AiToolUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiToolVO;

import java.util.List;

public interface AiToolService extends BaseService<
        ApiPage<AiToolVO>,
        AiToolPageDTO,
        AiToolAddDTO,
        AiToolUpdateDTO
        > {

    List<AiToolVO> listEnabled();

    List<AiToolVO> listByType(String type);
}
