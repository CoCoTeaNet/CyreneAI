package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptAbTestVO;

import java.math.BigInteger;

public interface AiPromptAbTestService extends BaseService<
        ApiPage<AiPromptAbTestVO>,
        AiPromptAbTestPageDTO,
        AiPromptAbTestAddDTO,
        AiPromptAbTestUpdateDTO
        > {

    AiPromptAbTestVO detail(BigInteger id);

    boolean changeStatus(BigInteger id, String status);
}
