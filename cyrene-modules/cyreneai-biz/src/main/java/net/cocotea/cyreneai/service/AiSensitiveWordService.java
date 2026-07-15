package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordAddDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordPageDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordUpdateDTO;
import net.cocotea.cyreneai.model.po.AiSensitiveWord;
import net.cocotea.cyreneai.model.vo.AiSensitiveWordVO;

import java.util.List;

public interface AiSensitiveWordService extends BaseService<
        ApiPage<AiSensitiveWordVO>,
        AiSensitiveWordPageDTO,
        AiSensitiveWordAddDTO,
        AiSensitiveWordUpdateDTO
        > {

    /**
     * 拉取所有启用中的敏感词(用于 ContentSafetyService 缓存)
     */
    List<AiSensitiveWord> listEnabled();
}
