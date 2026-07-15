package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiPromptPresetAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptPresetVO;

import java.util.List;

public interface AiPromptPresetService extends BaseService<
        ApiPage<AiPromptPresetVO>,
        AiPromptPresetPageDTO,
        AiPromptPresetAddDTO,
        AiPromptPresetUpdateDTO
        > {

    List<AiPromptPresetVO> listEnabled(String category);
}
