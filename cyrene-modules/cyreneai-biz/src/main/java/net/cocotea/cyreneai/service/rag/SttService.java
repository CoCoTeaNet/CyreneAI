package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.dto.AiSttModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiSttModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.SttTranscribeDTO;
import net.cocotea.cyreneai.model.vo.AiSttModelVO;
import net.cocotea.cyreneai.model.vo.AiSttRecordVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

public interface SttService {

    String transcribe(SttTranscribeDTO dto);

    String transcribeFile(InputStream fileStream, String fileName, BigInteger modelId);

    List<AiSttModelVO> listModels();

    boolean add(AiSttModelAddDTO dto);

    boolean update(AiSttModelUpdateDTO dto);

    boolean deleteBatch(List<BigInteger> idList);

    ApiPage<AiSttModelVO> listByPage(AiSttModelPageDTO pageDTO);

    List<AiSttModelVO> listEnabled();

    ApiPage<AiSttRecordVO> listHistoryByPage(AiSttModelPageDTO pageDTO);
}
