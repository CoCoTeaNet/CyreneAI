package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiPromptRenderDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateAddDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplatePageDTO;
import net.cocotea.cyreneai.model.dto.AiPromptTemplateUpdateDTO;
import net.cocotea.cyreneai.model.po.AiPromptTemplate;
import net.cocotea.cyreneai.model.po.AiPromptTemplateVersion;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVO;
import net.cocotea.cyreneai.model.vo.AiPromptTemplateVersionVO;
import net.cocotea.cyreneai.service.AiPromptTemplateService;
import net.cocotea.cyreneai.util.PromptTemplateRenderer;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Component
public class AiPromptTemplateServiceImpl implements AiPromptTemplateService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiPromptTemplateAddDTO param) {
        AiPromptTemplate po = lightDao.convertType(param, AiPromptTemplate.class);
        if (po.getSort() == null) po.setSort(0);
        if (po.getEnableStatus() == null) po.setEnableStatus(1);
        if (StrUtil.isBlank(po.getScene())) po.setScene("system");
        po.setCurrentVersion(1);
        Object o = lightDao.save(po);
        if (o == null) {
            return false;
        }
        // 同步生成 v1 版本历史
        saveVersion(po.getId(), 1, po.getContent(), po.getVariables(), param.getChangeNote());
        return true;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiPromptTemplateUpdateDTO param) {
        AiPromptTemplate existed = lightDao.load(new AiPromptTemplate(new BigInteger(param.getId())));
        if (existed == null) {
            return false;
        }
        AiPromptTemplate po = lightDao.convertType(param, AiPromptTemplate.class);
        // 若内容发生变化，则版本号 +1，并写入版本历史
        boolean contentChanged = param.getContent() != null && !param.getContent().equals(existed.getContent());
        boolean variablesChanged = param.getVariables() != null && !param.getVariables().equals(existed.getVariables());
        if (contentChanged || variablesChanged) {
            int newVersion = (existed.getCurrentVersion() == null ? 1 : existed.getCurrentVersion()) + 1;
            po.setCurrentVersion(newVersion);
            saveVersion(existed.getId(), newVersion,
                    param.getContent() != null ? param.getContent() : existed.getContent(),
                    param.getVariables() != null ? param.getVariables() : existed.getVariables(),
                    param.getChangeNote());
        }
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiPromptTemplateVO> listByPage(AiPromptTemplatePageDTO pageDTO) {
        AiPromptTemplatePageDTO.Query query = pageDTO.getAiPromptTemplate();
        Map<String, Object> map = MapUtil.newHashMap(4);
        map.put("name", query != null && query.getName() != null ? "%" + query.getName() + "%" : null);
        map.put("category", query != null ? query.getCategory() : null);
        map.put("scene", query != null ? query.getScene() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiPromptTemplateVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_prompt_template_findList", map, AiPromptTemplateVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiPromptTemplate po = new AiPromptTemplate().setId(id).setIsDeleted(1);
        Long update = lightDao.update(po);
        return update != null && update > 0;
    }

    @Override
    public List<AiPromptTemplateVO> listEnabled(String category, String scene) {
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("enableStatus", 1);
        map.put("category", category);
        map.put("scene", scene);
        return lightDao.find("ai_prompt_template_findList", map, AiPromptTemplateVO.class);
    }

    @Override
    public String render(AiPromptRenderDTO dto) {
        String content = dto.getContent();
        if (StrUtil.isBlank(content) && StrUtil.isNotBlank(dto.getTemplateId())) {
            BigInteger tid = new BigInteger(dto.getTemplateId());
            if (dto.getVersion() != null) {
                AiPromptTemplateVersionVO v = getVersion(tid, dto.getVersion());
                content = v != null ? v.getContent() : null;
            } else {
                AiPromptTemplate t = lightDao.load(new AiPromptTemplate(tid));
                content = t != null ? t.getContent() : null;
            }
        }
        if (StrUtil.isBlank(content)) {
            return "";
        }
        return PromptTemplateRenderer.render(content, dto.getVariables());
    }

    @Override
    public List<AiPromptTemplateVersionVO> listVersions(BigInteger templateId) {
        Map<String, Object> map = MapUtil.newHashMap(1);
        map.put("templateId", templateId);
        return lightDao.find("ai_prompt_template_version_findList", map, AiPromptTemplateVersionVO.class);
    }

    @Override
    public AiPromptTemplateVersionVO getVersion(BigInteger templateId, Integer version) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("templateId", templateId);
        map.put("version", version);
        List<AiPromptTemplateVersionVO> list = lightDao.find("ai_prompt_template_version_findList", map, AiPromptTemplateVersionVO.class);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public boolean rollback(BigInteger templateId, Integer version, String changeNote) {
        AiPromptTemplate existed = lightDao.load(new AiPromptTemplate(templateId));
        if (existed == null) {
            return false;
        }
        AiPromptTemplateVersionVO target = getVersion(templateId, version);
        if (target == null) {
            return false;
        }
        int newVersion = (existed.getCurrentVersion() == null ? 1 : existed.getCurrentVersion()) + 1;
        AiPromptTemplate patch = new AiPromptTemplate()
                .setId(templateId)
                .setContent(target.getContent())
                .setVariables(target.getVariables())
                .setCurrentVersion(newVersion);
        Long update = lightDao.update(patch);
        if (update != null && update > 0) {
            String note = StrUtil.isBlank(changeNote) ? ("Rollback to v" + version) : changeNote;
            saveVersion(templateId, newVersion, target.getContent(), target.getVariables(), note);
            return true;
        }
        return false;
    }

    private void saveVersion(BigInteger templateId, int version, String content, String variables, String changeNote) {
        AiPromptTemplateVersion v = new AiPromptTemplateVersion()
                .setTemplateId(templateId)
                .setVersion(version)
                .setContent(content)
                .setVariables(variables)
                .setChangeNote(changeNote);
        lightDao.save(v);
    }
}
