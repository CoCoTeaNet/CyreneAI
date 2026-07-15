package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiPromptEvalPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRateDTO;
import net.cocotea.cyreneai.model.dto.AiPromptEvalRunDTO;
import net.cocotea.cyreneai.model.vo.AiPromptEvalVO;

public interface AiPromptEvalService {

    /**
     * 运行一次评估：渲染 prompt、调用模型、记录 metrics。
     */
    AiPromptEvalVO run(AiPromptEvalRunDTO dto);

    /**
     * 提交人工评分/反馈。
     */
    boolean rate(AiPromptEvalRateDTO dto);

    ApiPage<AiPromptEvalVO> listByPage(AiPromptEvalPageDTO pageDTO);
}
