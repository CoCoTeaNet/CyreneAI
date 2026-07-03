package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.dto.AiImageModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiImageModelUpdateDTO;
import net.cocotea.cyreneai.model.dto.ImageGenerateDTO;
import net.cocotea.cyreneai.model.vo.AiImageModelVO;
import net.cocotea.cyreneai.model.vo.AiImageRecordVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

public interface ImageService {

    String generate(ImageGenerateDTO dto);

    List<AiImageModelVO> listModels();

    boolean add(AiImageModelAddDTO dto);

    boolean update(AiImageModelUpdateDTO dto);

    boolean deleteBatch(List<BigInteger> ids);

    ApiPage<AiImageModelVO> listByPage(AiImageModelPageDTO pageDTO);

    ApiPage<AiImageRecordVO> listHistoryByPage(AiImageModelPageDTO pageDTO);

    boolean deleteHistory(BigInteger id);
}
