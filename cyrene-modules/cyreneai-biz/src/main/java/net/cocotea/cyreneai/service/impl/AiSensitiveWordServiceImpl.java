package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordAddDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordPageDTO;
import net.cocotea.cyreneai.model.dto.AiSensitiveWordUpdateDTO;
import net.cocotea.cyreneai.model.po.AiSensitiveWord;
import net.cocotea.cyreneai.model.vo.AiSensitiveWordVO;
import net.cocotea.cyreneai.service.AiSensitiveWordService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiSensitiveWordServiceImpl implements AiSensitiveWordService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiSensitiveWordAddDTO param) {
        AiSensitiveWord word = lightDao.convertType(param, AiSensitiveWord.class);
        if (word.getEnableStatus() == null) word.setEnableStatus(1);
        if (word.getSort() == null) word.setSort(0);
        if (word.getCategory() == null) word.setCategory("custom");
        if (word.getTarget() == null) word.setTarget("both");
        if ("replace".equals(word.getStrategy()) && (word.getReplacement() == null || word.getReplacement().isEmpty())) {
            word.setReplacement("***");
        }
        Object saved = lightDao.save(word);
        return saved != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiSensitiveWordUpdateDTO param) {
        AiSensitiveWord word = lightDao.convertType(param, AiSensitiveWord.class);
        Long update = lightDao.update(word);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiSensitiveWordVO> listByPage(AiSensitiveWordPageDTO pageDTO) {
        AiSensitiveWordPageDTO.Query query = pageDTO.getAiSensitiveWord();
        Map<String, Object> map = MapUtil.newHashMap(5);
        map.put("word", query != null ? query.getWord() : null);
        map.put("category", query != null ? query.getCategory() : null);
        map.put("strategy", query != null ? query.getStrategy() : null);
        map.put("target", query != null ? query.getTarget() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiSensitiveWordVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_sensitive_word_findList", map, AiSensitiveWordVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiSensitiveWord word = new AiSensitiveWord().setId(id).setIsDeleted(1);
        Long update = lightDao.update(word);
        return update != null && update > 0;
    }

    @Override
    public List<AiSensitiveWord> listEnabled() {
        Map<String, Object> params = MapUtil.newHashMap(0);
        return lightDao.find("ai_sensitive_word_findEnabled", params, AiSensitiveWord.class);
    }
}
