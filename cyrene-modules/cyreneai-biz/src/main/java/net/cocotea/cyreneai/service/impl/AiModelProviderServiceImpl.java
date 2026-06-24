package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiModelProviderAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelProviderUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.vo.AiModelProviderVO;
import net.cocotea.cyreneai.service.AiModelProviderService;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AiModelProviderServiceImpl implements AiModelProviderService {

    private final LightDao lightDao;

    @Override
    public boolean add(AiModelProviderAddDTO param) {
        AiModelProvider provider = lightDao.convertType(param, AiModelProvider.class);
        if (provider.getSort() == null) {
            provider.setSort(0);
        }
        if (provider.getEnableStatus() == null) {
            provider.setEnableStatus(1);
        }
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
        Page<AiModelProviderVO> page = lightDao.findPage(
                ApiPage.create(pageDTO),
                "ai_model_provider_findList",
                map,
                AiModelProviderVO.class
        );
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiModelProvider provider = new AiModelProvider().setId(id).setIsDeleted(1);
        Long update = lightDao.update(provider);
        return update != null && update > 0;
    }
}
