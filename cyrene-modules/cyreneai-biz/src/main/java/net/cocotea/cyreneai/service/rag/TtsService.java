package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.dto.AiTtsModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiTtsModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.TtsSynthesizeDTO;
import net.cocotea.cyreneai.model.vo.AiTtsModelVO;
import net.cocotea.cyreneai.model.vo.AiTtsRecordVO;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import java.math.BigInteger;
import java.util.List;

public interface TtsService {

    byte[] synthesize(TtsSynthesizeDTO dto);

    List<AiTtsModelVO> listModels();

    boolean add(AiTtsModelAddDTO dto);

    boolean update(AiTtsModelUpdateDTO dto);

    boolean deleteBatch(List<BigInteger> ids);

    ApiPage<AiTtsModelVO> listByPage(AiTtsModelPageDTO pageDTO);

    ApiPage<AiTtsRecordVO> listHistoryByPage(ApiPageDTO pageDTO);
}
