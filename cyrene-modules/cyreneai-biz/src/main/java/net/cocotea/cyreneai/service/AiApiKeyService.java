package net.cocotea.cyreneai.service;

import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.service.BaseService;
import net.cocotea.cyreneai.model.dto.AiApiKeyAddDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyPageDTO;
import net.cocotea.cyreneai.model.dto.AiApiKeyUpdateDTO;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.model.vo.AiApiKeyUsageVO;
import net.cocotea.cyreneai.model.vo.AiApiKeyVO;

import java.math.BigInteger;
import java.util.List;

public interface AiApiKeyService extends BaseService<
        ApiPage<AiApiKeyVO>,
        AiApiKeyPageDTO,
        AiApiKeyAddDTO,
        AiApiKeyUpdateDTO
        > {

    /**
     * 生成新 Key 并返回明文(仅本次)
     */
    AiApiKeyVO generate(AiApiKeyAddDTO param);

    /**
     * 校验明文 Key 并返回对应记录; 校验失败返回 null
     */
    AiApiKey verifyPlainKey(String plainKey);

    /**
     * 累计 Key 用量并更新 last_used_time
     */
    void recordUsage(BigInteger apiKeyId, BigInteger userId, int promptTokens, int completionTokens,
                     java.math.BigDecimal cost, String status);

    /**
     * 拉取指定 Key 最近 N 天用量记录
     */
    List<AiApiKeyUsageVO> statRecent(BigInteger apiKeyId, Integer days);
}
