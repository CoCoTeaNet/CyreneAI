package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiModelAddDTO;
import net.cocotea.cyreneai.model.dto.AiModelPageDTO;
import net.cocotea.cyreneai.model.dto.AiModelUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.vo.AiModelVO;
import net.cocotea.cyreneai.service.AiModelService;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class AiModelServiceImpl implements AiModelService {

    private final LightDao lightDao;

    @Override
    public boolean add(AiModelAddDTO param) {
        AiModel model = lightDao.convertType(param, AiModel.class);
        if (model.getSort() == null) {
            model.setSort(0);
        }
        if (model.getEnableStatus() == null) {
            model.setEnableStatus(1);
        }
        Object o = lightDao.save(model);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiModelUpdateDTO param) {
        AiModel model = lightDao.convertType(param, AiModel.class);
        Long update = lightDao.update(model);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiModelVO> listByPage(AiModelPageDTO pageDTO) {
        AiModelPageDTO.Query query = pageDTO.getAiModel();
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("providerId", query != null ? query.getProviderId() : null);
        map.put("modelName", query != null ? query.getModelName() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiModelVO> page = lightDao.findPage(
                ApiPage.create(pageDTO),
                "ai_model_findList",
                map,
                AiModelVO.class
        );
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiModel model = new AiModel().setId(id).setIsDeleted(1);
        Long update = lightDao.update(model);
        return update != null && update > 0;
    }
}
