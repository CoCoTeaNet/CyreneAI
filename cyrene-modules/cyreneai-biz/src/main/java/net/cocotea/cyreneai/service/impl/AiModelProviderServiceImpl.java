package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;
import net.cocotea.cyreneai.service.AiModelProviderService;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiModelProviderServiceImpl implements AiModelProviderService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiModelProviderAddDTO param) {
        AiModelProvider provider = lightDao.convertType(param, AiModelProvider.class);
        if (provider.getSort() == null) {
            provider.setSort(0);
        }
        if (provider.getEnableStatus() == null) {
            provider.setEnableStatus(1);
        }
        // 密钥加密存储
        provider.setApiKey(ApiKeyCipher.encrypt(provider.getApiKey()));
        Object o = lightDao.save(provider);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiModelProviderUpdateDTO param) {
        AiModelProvider provider = lightDao.convertType(param, AiModelProvider.class);
        // 密钥处理：空值或脱敏占位（未修改）时保留原密钥，否则加密新密钥
        String incoming = provider.getApiKey();
        if (incoming == null || incoming.isBlank() || ApiKeyCipher.isMasked(incoming)) {
            AiModelProvider exist = lightDao.load(new AiModelProvider(provider.getId()));
            provider.setApiKey(exist != null ? exist.getApiKey() : null);
        } else {
            provider.setApiKey(ApiKeyCipher.encrypt(incoming));
        }
        Long update = lightDao.update(provider);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiModelProviderVO> listByPage(AiModelProviderPageDTO pageDTO) {
        AiModelProviderPageDTO.Query query = pageDTO.getAiModelProvider();
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("providerName", query != null ? query.getProviderName() : null);
        map.put("providerType", query != null ? query.getProviderType() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiModelProviderVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_model_provider_findList", map, AiModelProviderVO.class);
        // 密钥脱敏展示（sk-****...ab12）
        if (page.getRows() != null) {
            page.getRows().forEach(vo -> vo.setApiKey(ApiKeyCipher.mask(ApiKeyCipher.decrypt(vo.getApiKey()))));
        }
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiModelProvider provider = new AiModelProvider().setId(id).setIsDeleted(1);
        Long update = lightDao.update(provider);
        return update != null && update > 0;
    }

    @Override
    public int rotateSecret(String oldSecret, String newSecret) {
        List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                EntityQuery.create().where("is_deleted = 0"));
        int count = 0;
        for (AiModelProvider provider : providers) {
            String stored = provider.getApiKey();
            if (stored == null || stored.isBlank()) {
                continue;
            }
            String plain = ApiKeyCipher.decryptWith(stored, oldSecret);
            String reEncrypted = ApiKeyCipher.encryptWith(plain, newSecret);
            AiModelProvider update = new AiModelProvider(provider.getId());
            update.setApiKey(reEncrypted);
            Long updated = lightDao.update(update);
            if (updated != null && updated > 0) {
                count++;
            }
        }
        return count;
    }
}
