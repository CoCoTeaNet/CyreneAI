package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiPromptRenderDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplatePageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVO;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVersionVO;

import java.math.BigInteger;
import java.util.List;

public interface AiPromptTemplateService extends BaseService<
        ApiPage<AiPromptTemplateVO>,
        AiPromptTemplatePageDTO,
        AiPromptTemplateAddDTO,
        AiPromptTemplateUpdateDTO
        > {

    List<AiPromptTemplateVO> listEnabled(String category, String scene);

    /**
     * 使用变量渲染模板内容。
     */
    String render(AiPromptRenderDTO dto);

    /**
     * 查询指定模板的版本历史列表。
     */
    List<AiPromptTemplateVersionVO> listVersions(BigInteger templateId);

    /**
     * 获取指定版本内容。
     */
    AiPromptTemplateVersionVO getVersion(BigInteger templateId, Integer version);

    /**
     * 将模板回滚到指定历史版本。
     */
    boolean rollback(BigInteger templateId, Integer version, String changeNote);
}
