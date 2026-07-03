package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.dto.AiVisionModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiVisionModelVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface VisionService {

    boolean add(AiVisionModelAddDTO dto);

    boolean update(AiVisionModelUpdateDTO dto);

    boolean deleteBatch(List<BigInteger> ids);

    ApiPage<AiVisionModelVO> listByPage(AiVisionModelPageDTO pageDTO);

    List<AiVisionModelVO> listEnabledModels();
}
