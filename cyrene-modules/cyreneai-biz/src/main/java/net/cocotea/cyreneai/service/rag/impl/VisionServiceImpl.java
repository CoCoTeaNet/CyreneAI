package net.cocotea.cyreneai.service.rag.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.AiVisionModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiVisionModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import net.cocotea.cyreneai.model.vo.AiVisionModelVO;
import net.cocotea.cyreneai.service.rag.VisionService;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VisionServiceImpl implements VisionService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiVisionModelAddDTO dto) {
        AiModelProvider provider = getProviderByType(dto.getProviderType());
        if (provider == null) return false;
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        model.setModelType("vision");
        model.setProviderId(provider.getId());
        return lightDao.save(model) != null;
    }

    @Override
    public boolean update(AiVisionModelUpdateDTO dto) {
        AiModelProvider provider = getProviderByType(dto.getProviderType());
        if (provider == null) return false;
        AiModel model = BeanUtil.copyProperties(dto, AiModel.class);
        model.setModelType("vision");
        model.setProviderId(provider.getId());
        Long updated = lightDao.update(model);
        return updated != null && updated > 0;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> ids) {
        if (ids == null || ids.isEmpty()) return false;
        for (BigInteger id : ids) {
            AiModel model = new AiModel(id);
            model.setIsDeleted(1);
            lightDao.update(model);
        }
        return true;
    }

    @Override
    public ApiPage<AiVisionModelVO> listByPage(AiVisionModelPageDTO pageDTO) {
        Map<String, Object> params = MapUtil.newHashMap(3);
        AiVisionModelPageDTO.Query q = pageDTO.getVisionModel();
        params.put("providerType", q != null ? q.getProviderType() : null);
        params.put("modelName", q != null ? q.getModelName() : null);
        params.put("enableStatus", q != null ? q.getEnableStatus() : null);
        Page<AiVisionModelVO> pageParam = new Page<>();
        pageParam.setPageNo(pageDTO.getPageNo());
        pageParam.setPageSize(pageDTO.getPageSize());
        Page<AiVisionModelVO> page = lightDao.findPage(pageParam, "ai_vision_model_findList", params, AiVisionModelVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public List<AiVisionModelVO> listEnabledModels() {
        EntityQuery eq = EntityQuery.create()
                .where("model_type = 'vision' and enable_status = 1 and is_deleted = 0")
                .orderByDesc("sort");
        return lightDao.findEntity(AiModel.class, eq).stream()
                .map(m -> {
                    AiModelProvider provider = getProvider(m.getProviderId());
                    AiVisionModelVO vo = new AiVisionModelVO();
                    vo.setId(m.getId());
                    vo.setProviderType(provider != null ? provider.getProviderType() : null);
                    vo.setModelName(m.getModelName());
                    vo.setApiKey(provider != null ? ApiKeyCipher.mask(ApiKeyCipher.decrypt(provider.getApiKey())) : null);
                    vo.setApiBaseUrl(provider != null ? provider.getApiBaseUrl() : null);
                    vo.setIsDefault(m.getIsDefault());
                    vo.setEnableStatus(m.getEnableStatus());
                    vo.setSort(m.getSort());
                    vo.setRemark(m.getRemark());
                    vo.setCreateBy(m.getCreateBy());
                    vo.setCreateTime(m.getCreateTime());
                    vo.setUpdateBy(m.getUpdateBy());
                    vo.setUpdateTime(m.getUpdateTime());
                    return vo;
                }).toList();
    }

    private AiModelProvider getProvider(BigInteger providerId) {
        return lightDao.load(new AiModelProvider(providerId));
    }

    private AiModelProvider getProviderByType(String providerType) {
        List<AiModelProvider> providers = lightDao.findEntity(AiModelProvider.class,
                EntityQuery.create().where("provider_type = ? and is_deleted = 0")
                        .values(providerType));
        return providers.isEmpty() ? null : providers.getFirst();
    }
}
