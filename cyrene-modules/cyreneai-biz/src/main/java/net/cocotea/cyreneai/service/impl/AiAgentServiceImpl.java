package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiAgentAddDTO;
import net.cocotea.cyreneai.model.dto.AiAgentPageDTO;
import net.cocotea.cyreneai.model.dto.AiAgentUpdateDTO;
import net.cocotea.cyreneai.model.po.AiAgent;
import net.cocotea.cyreneai.model.po.AiTool;
import net.cocotea.cyreneai.model.vo.AiAgentVO;
import net.cocotea.cyreneai.model.vo.AiToolVO;
import net.cocotea.cyreneai.service.AiAgentService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AiAgentServiceImpl implements AiAgentService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiAgentAddDTO param) {
        AiAgent agent = lightDao.convertType(param, AiAgent.class);
        if (agent.getSort() == null) agent.setSort(0);
        if (agent.getEnableStatus() == null) agent.setEnableStatus(1);
        if (agent.getMaxIterations() == null) agent.setMaxIterations(10);
        // 将toolIds列表转为JSON字符串
        if (param.getToolIds() != null && !param.getToolIds().isEmpty()) {
            agent.setToolIds(JSONUtil.toJsonStr(param.getToolIds()));
        }
        Object o = lightDao.save(agent);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiAgentUpdateDTO param) {
        AiAgent agent = lightDao.convertType(param, AiAgent.class);
        if (param.getToolIds() != null && !param.getToolIds().isEmpty()) {
            agent.setToolIds(JSONUtil.toJsonStr(param.getToolIds()));
        }
        Long update = lightDao.update(agent);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiAgentVO> listByPage(AiAgentPageDTO pageDTO) {
        AiAgentPageDTO.Query query = pageDTO.getAiAgent();
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("name", query != null ? query.getName() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiAgentVO> page = lightDao.findPage(
                ApiPage.create(pageDTO),
                "ai_agent_findList",
                map,
                AiAgentVO.class
        );
        // Enrich with tool details
        for (AiAgentVO vo : page.getRows()) {
            enrichAgentVO(vo);
        }
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiAgent agent = new AiAgent().setId(id).setIsDeleted(1);
        Long update = lightDao.update(agent);
        return update != null && update > 0;
    }

    @Override
    public List<AiAgentVO> listEnabled() {
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("enableStatus", 1);
        List<AiAgentVO> list = lightDao.find("ai_agent_findList", map, AiAgentVO.class);
        for (AiAgentVO vo : list) {
            enrichAgentVO(vo);
        }
        return list;
    }

    private void enrichAgentVO(AiAgentVO vo) {
        if (vo.getToolIds() != null && !vo.getToolIds().isBlank()) {
            try {
                List<String> toolIdStrs = JSONUtil.parseArray(vo.getToolIds())
                        .toList(String.class);
                List<AiToolVO> tools = new ArrayList<>();
                for (String idStr : toolIdStrs) {
                    try {
                        AiTool tool = lightDao.load(new AiTool(new BigInteger(idStr)));
                        if (tool != null && tool.getIsDeleted() == 0) {
                            AiToolVO toolVO = lightDao.convertType(tool, AiToolVO.class);
                            tools.add(toolVO);
                        }
                    } catch (Exception ignored) {}
                }
                vo.setTools(tools);
            } catch (Exception ignored) {}
        } else {
            vo.setTools(Collections.emptyList());
        }
    }
}
