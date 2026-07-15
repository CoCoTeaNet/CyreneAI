package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiPromptPresetAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptPresetUpdateDTO;
import net.cocotea.cyreneai.model.po.AiPromptPreset;
import net.cocotea.cyreneai.model.vo.AiPromptPresetVO;
import net.cocotea.cyreneai.service.AiPromptPresetService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiPromptPresetServiceImpl implements AiPromptPresetService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiPromptPresetAddDTO param) {
        AiPromptPreset po = lightDao.convertType(param, AiPromptPreset.class);
        if (po.getSort() == null) po.setSort(0);
        if (po.getEnableStatus() == null) po.setEnableStatus(1);
        if (po.getIsBuiltin() == null) po.setIsBuiltin(0);
        Object o = lightDao.save(po);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiPromptPresetUpdateDTO param) {
        AiPromptPreset po = lightDao.convertType(param, AiPromptPreset.class);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiPromptPresetVO> listByPage(AiPromptPresetPageDTO pageDTO) {
        AiPromptPresetPageDTO.Query query = pageDTO.getAiPromptPreset();
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("name", query != null && query.getName() != null ? "%" + query.getName() + "%" : null);
        map.put("category", query != null ? query.getCategory() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiPromptPresetVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_prompt_preset_findList", map, AiPromptPresetVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        // 内置预设不允许删除
        AiPromptPreset existed = lightDao.load(new AiPromptPreset(id));
        if (existed != null && existed.getIsBuiltin() != null && existed.getIsBuiltin() == 1) {
            return false;
        }
        AiPromptPreset po = new AiPromptPreset().setId(id).setIsDeleted(1);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public List<AiPromptPresetVO> listEnabled(String category) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("enableStatus", 1);
        map.put("category", category);
        return lightDao.find("ai_prompt_preset_findList", map, AiPromptPresetVO.class);
    }
}
