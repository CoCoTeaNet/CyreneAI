package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneai.model.dto.AiBudgetAddDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetPageDTO;
import net.cocotea.cyreneai.model.dto.AiBudgetUpdateDTO;
import net.cocotea.cyreneai.model.po.AiBudget;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.vo.AiBudgetStatusVO;
import net.cocotea.cyreneai.model.vo.AiBudgetVO;
import net.cocotea.cyreneai.service.AiBudgetService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class AiBudgetServiceImpl implements AiBudgetService {

    @Db
    private LightDao lightDao;

    @Override
    public boolean add(AiBudgetAddDTO param) {
        AiBudget budget = lightDao.convertType(param, AiBudget.class);
        if (budget.getScopeType() == null) budget.setScopeType("global");
        if (budget.getPeriod() == null) budget.setPeriod("month");
        if (budget.getAlertThreshold() == null) budget.setAlertThreshold(new BigDecimal("0.8"));
        if (budget.getEnableStatus() == null) budget.setEnableStatus(1);
        Object saved = lightDao.save(budget);
        return saved != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(AiBudgetUpdateDTO param) {
        AiBudget budget = lightDao.convertType(param, AiBudget.class);
        Long update = lightDao.update(budget);
        return update != null && update > 0;
    }

    @Override
    public ApiPage<AiBudgetVO> listByPage(AiBudgetPageDTO pageDTO) {
        AiBudgetPageDTO.Query query = pageDTO.getAiBudget();
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("name", query != null ? query.getName() : null);
        map.put("scopeType", query != null ? query.getScopeType() : null);
        map.put("enableStatus", query != null ? query.getEnableStatus() : null);
        Page<AiBudgetVO> page = lightDao.findPage(ApiPage.create(pageDTO), "ai_budget_findList", map, AiBudgetVO.class);
        return ApiPage.rest(page);
    }

    @Override
    public boolean delete(BigInteger id) {
        AiBudget budget = new AiBudget().setId(id).setIsDeleted(1);
        Long update = lightDao.update(budget);
        return update != null && update > 0;
    }

    @Override
    public List<AiBudgetStatusVO> listStatus() {
        Map<String, Object> listParam = MapUtil.newHashMap(1);
        listParam.put("enableStatus", 1);
        List<AiBudgetVO> budgets = lightDao.find("ai_budget_findList", listParam, AiBudgetVO.class);
        return budgets.stream().map(this::toStatus).toList();
    }

    private AiBudgetStatusVO toStatus(AiBudgetVO b) {
        LocalDateTime periodStart = periodStart(b.getPeriod());
        Map<String, Object> costParam = MapUtil.newHashMap(3);
        costParam.put("startTime", periodStart);
        if ("model".equals(b.getScopeType())) {
            costParam.put("modelId", b.getScopeId());
        } else if ("user".equals(b.getScopeType())) {
            costParam.put("userId", b.getScopeId());
        }
        List<Map> costRows = lightDao.find("ai_budget_used_cost", costParam, Map.class);
        BigDecimal usedCost = BigDecimal.ZERO;
        if (costRows != null && !costRows.isEmpty() && costRows.getFirst().get("usedCost") != null) {
            usedCost = new BigDecimal(costRows.getFirst().get("usedCost").toString());
        }

        BigDecimal amount = b.getAmount() != null ? b.getAmount() : BigDecimal.ZERO;
        BigDecimal usagePercent = BigDecimal.ZERO;
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            usagePercent = usedCost.multiply(BigDecimal.valueOf(100))
                    .divide(amount, 2, RoundingMode.HALF_UP);
        }
        BigDecimal threshold = b.getAlertThreshold() != null ? b.getAlertThreshold() : new BigDecimal("0.8");
        boolean exceeded = amount.compareTo(BigDecimal.ZERO) > 0 && usedCost.compareTo(amount) >= 0;
        boolean alerting = !exceeded && amount.compareTo(BigDecimal.ZERO) > 0
                && usedCost.compareTo(amount.multiply(threshold)) >= 0;

        return new AiBudgetStatusVO()
                .setId(b.getId())
                .setName(b.getName())
                .setScopeType(b.getScopeType())
                .setScopeId(b.getScopeId())
                .setScopeName(resolveScopeName(b.getScopeType(), b.getScopeId()))
                .setPeriod(b.getPeriod())
                .setAmount(amount)
                .setUsedCost(usedCost)
                .setUsagePercent(usagePercent)
                .setAlertThreshold(threshold)
                .setExceeded(exceeded)
                .setAlerting(alerting)
                .setPeriodStart(periodStart);
    }

    private String resolveScopeName(String scopeType, BigInteger scopeId) {
        if ("global".equals(scopeType) || scopeId == null) {
            return "全局";
        }
        if ("model".equals(scopeType)) {
            AiModel model = lightDao.load(new AiModel(scopeId));
            return model != null && model.getModelName() != null ? model.getModelName() : "模型#" + scopeId;
        }
        return "用户#" + scopeId;
    }

    private LocalDateTime periodStart(String period) {
        LocalDate today = LocalDate.now();
        return switch (period == null ? "month" : period) {
            case "day" -> today.atStartOfDay();
            case "week" -> today.with(DayOfWeek.MONDAY).atStartOfDay();
            default -> today.withDayOfMonth(1).atStartOfDay();
        };
    }
}
