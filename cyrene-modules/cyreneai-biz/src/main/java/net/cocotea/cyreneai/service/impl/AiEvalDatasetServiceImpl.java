package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetAddDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetPageDTO;
import net.cocotea.cyreneai.model.dto.AiEvalDatasetUpdateDTO;
import net.cocotea.cyreneai.model.po.AiEvalDataset;
import net.cocotea.cyreneai.model.vo.AiEvalDatasetVO;
import net.cocotea.cyreneai.service.AiEvalDatasetService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiEvalDatasetServiceImpl implements AiEvalDatasetService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiEvalDatasetAddDTO param) {
        AiEvalDataset dataset = lightDao.convertType(param, AiEvalDataset.class);
        if (dataset.getCategory() == null) dataset.setCategory("general");
        if (dataset.getEnableStatus() == null) dataset.setEnableStatus(1);
        if (dataset.getSort() == null) dataset.setSort(0);
        dataset.setItemCount(countItems(dataset.getItemsJson()));
        Object saved = lightDao.save(dataset);
        return saved != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiEvalDatasetUpdateDTO param) {
        AiEvalDataset dataset = lightDao.convertType(param, AiEvalDataset.class);
        dataset.setItemCount(countItems(dataset.getItemsJson()));
        Long update = lightDao.update(dataset);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiEvalDatasetVO> listByPage(AiEvalDatasetPageDTO pageDTO) {
        AiEvalDatasetPageDTO.Query query = pageDTO.getAiEvalDataset();
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("name", query != null ? query.getName() : null);
        map.put("category", query != null ? query.getCategory() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiEvalDatasetVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_eval_dataset_findList", map, AiEvalDatasetVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiEvalDataset dataset = new AiEvalDataset().setId(id).setIsDeleted(1);
        Long update = lightDao.update(dataset);
        return update != null && update > 0;
    }

    /**
     * 解析 items_json (JSON 数组) 计算条目数, 解析失败返回 0
     */
    private Integer countItems(String itemsJson) {
        if (StrUtil.isBlank(itemsJson)) {
            return 0;
        }
        try {
            return JSONUtil.parseArray(itemsJson).size();
        } catch (Exception e) {
            return 0;
        }
    }
}
