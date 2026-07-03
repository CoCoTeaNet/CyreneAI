package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.agent.ToolExecutionService;
import net.cocotea.cyreneai.model.dto.AiToolAddDTO;
import net.cocotea.cyreneai.model.dto.AiToolPageDTO;
import net.cocotea.cyreneai.model.dto.AiToolUpdateDTO;
import net.cocotea.cyreneai.model.po.AiTool;
import net.cocotea.cyreneai.model.vo.AiToolVO;
import net.cocotea.cyreneai.service.AiToolService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiToolServiceImpl implements AiToolService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiToolAddDTO param) {
        AiTool tool = lightDao.convertType(param, AiTool.class);
        if (tool.getSort() == null) tool.setSort(0);
        if (tool.getEnableStatus() == null) tool.setEnableStatus(1);
        if (tool.getHttpMethod() == null) tool.setHttpMethod("POST");
        Object o = lightDao.save(tool);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiToolUpdateDTO param) {
        AiTool tool = lightDao.convertType(param, AiTool.class);
        Long update = lightDao.update(tool);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiToolVO> listByPage(AiToolPageDTO pageDTO) {
        AiToolPageDTO.Query query = pageDTO.getAiTool();
        Map<String, Object> map = MapUtil.newHashMap(4);
        map.put("name", query != null ? query.getName() : null);
        map.put("type", query != null ? query.getType() : null);
        map.put("builtinHandler", query != null ? query.getBuiltinHandler() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiToolVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_tool_findList", map, AiToolVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiTool tool = new AiTool().setId(id).setIsDeleted(1);
        Long update = lightDao.update(tool);
        return update != null && update > 0;
    }

    @Override
    public List<AiToolVO> listEnabled() {
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("enableStatus", 1);
        return lightDao.find("ai_tool_findList", map, AiToolVO.class);
    }

    @Override
    public List<AiToolVO> listByType(String type) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("type", type);
        map.put("enableStatus", 1);
        return lightDao.find("ai_tool_findList", map, AiToolVO.class);
    }
}
