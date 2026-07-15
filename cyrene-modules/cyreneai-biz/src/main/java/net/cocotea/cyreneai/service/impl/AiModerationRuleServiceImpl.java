package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiModerationRuleAddDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRulePageDTO;
import net.cocotea.cyreneai.model.dto.AiModerationRuleUpdateDTO;
import net.cocotea.cyreneai.model.po.AiModerationRule;
import net.cocotea.cyreneai.model.vo.AiModerationRuleVO;
import net.cocotea.cyreneai.service.AiModerationRuleService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiModerationRuleServiceImpl implements AiModerationRuleService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiModerationRuleAddDTO param) {
        AiModerationRule rule = lightDao.convertType(param, AiModerationRule.class);
        if (rule.getEnableStatus() == null) rule.setEnableStatus(1);
        if (rule.getSort() == null) rule.setSort(0);
        if (rule.getAction() == null) rule.setAction("block");
        if (rule.getTarget() == null) rule.setTarget("both");
        Object saved = lightDao.save(rule);
        return saved != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiModerationRuleUpdateDTO param) {
        AiModerationRule rule = lightDao.convertType(param, AiModerationRule.class);
        Long update = lightDao.update(rule);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiModerationRuleVO> listByPage(AiModerationRulePageDTO pageDTO) {
        AiModerationRulePageDTO.Query query = pageDTO.getAiModerationRule();
        Map<String, Object> map = MapUtil.newHashMap(4);
        map.put("name", query != null ? query.getName() : null);
        map.put("provider", query != null ? query.getProvider() : null);
        map.put("target", query != null ? query.getTarget() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiModerationRuleVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_moderation_rule_findList", map, AiModerationRuleVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiModerationRule rule = new AiModerationRule().setId(id).setIsDeleted(1);
        Long update = lightDao.update(rule);
        return update != null && update > 0;
    }

    @Override
    public List<AiModerationRule> listEnabled() {
        return lightDao.find("ai_moderation_rule_findEnabled", MapUtil.newHashMap(0), AiModerationRule.class);
    }
}
