package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;

public interface AiModelProviderService extends BaseService<
        ApiPage<AiModelProviderVO>,
        AiModelProviderPageDTO,
        AiModelProviderAddDTO,
        AiModelProviderUpdateDTO
        > {

    /**
     * 密钥轮换：用旧主密钥解密全部提供商密钥，再用新主密钥重新加密。
     * 轮换后需将配置 {@code myapp.ai.api-key-secret} 更新为新密钥。
     *
     * @return 成功轮换的记录数
     */
    int rotateSecret(String oldSecret, String newSecret);
}
