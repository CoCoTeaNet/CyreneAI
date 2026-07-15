package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestPageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptAbTestUpdateDTO;
import net.cocotea.cyreneai.model.po.AiPromptAbTest;
import net.cocotea.cyreneai.model.vo.AiPromptAbTestVO;
import net.cocotea.cyreneai.service.AiPromptAbTestService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiPromptAbTestServiceImpl implements AiPromptAbTestService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiPromptAbTestAddDTO param) {
        AiPromptAbTest po = lightDao.convertType(param, AiPromptAbTest.class);
        if (po.getTrafficSplit() == null) po.setTrafficSplit(50);
        if (po.getStatus() == null) po.setStatus("running");
        Object o = lightDao.save(po);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiPromptAbTestUpdateDTO param) {
        AiPromptAbTest po = lightDao.convertType(param, AiPromptAbTest.class);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiPromptAbTestVO> listByPage(AiPromptAbTestPageDTO pageDTO) {
        AiPromptAbTestPageDTO.Query query = pageDTO.getAiPromptAbTest();
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("name", query != null && query.getName() != null ? "%" + query.getName() + "%" : null);
        map.put("status", query != null ? query.getStatus() : null);
        Page<AiPromptAbTestVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_prompt_ab_test_findList", map, AiPromptAbTestVO.class);
        // 统计数据
        for (AiPromptAbTestVO vo : page.getRows()) {
            enrichStat(vo);
        }
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiPromptAbTest po = new AiPromptAbTest().setId(id).setIsDeleted(1);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public AiPromptAbTestVO detail(BigInteger id) {
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("id", id);
        // 复用 findList，通过 id 查询 -- 简单起见走 load + 手工封装
        AiPromptAbTest po = lightDao.load(new AiPromptAbTest(id));
        if (po == null || po.getIsDeleted() == 1) {
            return null;
        }
        AiPromptAbTestVO vo = lightDao.convertType(po, AiPromptAbTestVO.class);
        enrichStat(vo);
        return vo;
    }

    @Override
    public boolean changeStatus(BigInteger id, String status) {
        AiPromptAbTest po = new AiPromptAbTest().setId(id).setStatus(status);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @SuppressWarnings("unchecked")
    private void enrichStat(AiPromptAbTestVO vo) {
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("abTestId", vo.getId());
        List<Map<String, Object>> stats = (List<Map<String, Object>>) (List<?>) lightDao.find("ai_prompt_ab_test_stat", map, HashMap.class);
        vo.setSampleCountA(0L);
        vo.setSampleCountB(0L);
        vo.setAvgRatingA(0d);
        vo.setAvgRatingB(0d);
        for (Map<String, Object> row : stats) {
            Object variant = row.get("variant");
            Object cnt = row.get("sampleCount");
            if (cnt == null) cnt = row.get("sample_count");
            Object avg = row.get("avgRating");
            if (avg == null) avg = row.get("avg_rating");
            long count = cnt == null ? 0L : Long.parseLong(cnt.toString());
            double rating = avg == null ? 0d : Double.parseDouble(avg.toString());
            if ("A".equalsIgnoreCase(String.valueOf(variant))) {
                vo.setSampleCountA(count);
                vo.setAvgRatingA(rating);
            } else if ("B".equalsIgnoreCase(String.valueOf(variant))) {
                vo.setSampleCountB(count);
                vo.setAvgRatingB(rating);
            }
        }
    }
}
