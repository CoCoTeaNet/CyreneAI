package net.cocotea.cyreneai.service.impl;

import cn.hutool.core.map.MapUtil;
import net.cocotea.cyreneai.model.vo.AiCostStatVO;
import net.cocotea.cyreneai.model.vo.AiCostSuggestionVO;
import net.cocotea.cyreneai.model.vo.AiModelRankVO;
import net.cocotea.cyreneai.model.vo.AiMonitorOverviewVO;
import net.cocotea.cyreneai.model.vo.AiTokenTrendVO;
import net.cocotea.cyreneai.model.vo.AiUserRankVO;
import net.cocotea.cyreneai.service.AiMonitorService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class AiMonitorServiceImpl implements AiMonitorService {

    @Db
    private LightDao lightDao;

    @Override
    public AiMonitorOverviewVO overview(Integer days) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("startTime", startTime(days));
        map.put("endTime", null);
        List<AiMonitorOverviewVO> list = lightDao.find("ai_monitor_overview", map, AiMonitorOverviewVO.class);
        AiMonitorOverviewVO vo = (list != null && !list.isEmpty()) ? list.getFirst() : new AiMonitorOverviewVO();
        if (vo.getRequestCount() == null) vo.setRequestCount(0L);
        if (vo.getTotalTokens() == null) vo.setTotalTokens(0L);
        if (vo.getTotalCost() == null) vo.setTotalCost(BigDecimal.ZERO);
        if (vo.getAvgLatency() == null) vo.setAvgLatency(0D);
        if (vo.getSuccessCount() == null) vo.setSuccessCount(0L);
        // 计算成功率(0-100)
        if (vo.getRequestCount() > 0) {
            vo.setSuccessRate(BigDecimal.valueOf(vo.getSuccessCount())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(vo.getRequestCount()), 2, RoundingMode.HALF_UP));
        } else {
            vo.setSuccessRate(BigDecimal.ZERO);
        }
        return vo;
    }

    @Override
    public List<AiTokenTrendVO> tokenTrend(String groupType, Integer days) {
        Map<String, Object> map = MapUtil.newHashMap(3);
        map.put("dateFormat", dateFormat(groupType));
        map.put("startTime", startTime(days));
        map.put("endTime", null);
        return lightDao.find("ai_monitor_token_trend", map, AiTokenTrendVO.class);
    }

    @Override
    public List<AiModelRankVO> modelRank(Integer days) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("startTime", startTime(days));
        map.put("endTime", null);
        return lightDao.find("ai_monitor_model_rank", map, AiModelRankVO.class);
    }

    @Override
    public List<AiUserRankVO> userRank(Integer days) {
        Map<String, Object> map = MapUtil.newHashMap(2);
        map.put("startTime", startTime(days));
        map.put("endTime", null);
        return lightDao.find("ai_monitor_user_rank", map, AiUserRankVO.class);
    }

    @Override
    public List<AiCostStatVO> costStat(String dimension, Integer days) {
        List<AiCostStatVO> result = new ArrayList<>();
        String dim = dimension == null ? "model" : dimension;
        switch (dim) {
            case "user" -> {
                for (AiUserRankVO u : userRank(days)) {
                    result.add(new AiCostStatVO()
                            .setDimKey(u.getUserId() != null ? u.getUserId().toString() : "")
                            .setDimName(u.getUserName() != null ? u.getUserName() : "未知用户")
                            .setCost(u.getCost())
                            .setTotalTokens(u.getTotalTokens())
                            .setRequestCount(u.getRequestCount()));
                }
            }
            case "time" -> {
                for (AiTokenTrendVO t : tokenTrend("day", days)) {
                    result.add(new AiCostStatVO()
                            .setDimKey(t.getPeriod())
                            .setDimName(t.getPeriod())
                            .setCost(t.getCost())
                            .setTotalTokens(t.getTotalTokens())
                            .setRequestCount(t.getRequestCount()));
                }
            }
            default -> {
                for (AiModelRankVO m : modelRank(days)) {
                    result.add(new AiCostStatVO()
                            .setDimKey(m.getModelId() != null ? m.getModelId().toString() : "")
                            .setDimName(m.getModelName() != null ? m.getModelName() : "未知模型")
                            .setCost(m.getCost())
                            .setTotalTokens(m.getTotalTokens())
                            .setRequestCount(m.getRequestCount()));
                }
            }
        }
        return result;
    }

    @Override
    public List<AiCostSuggestionVO> costSuggestions(Integer days) {
        List<AiCostSuggestionVO> suggestions = new ArrayList<>();
        AiMonitorOverviewVO overview = overview(days);
        List<AiModelRankVO> models = modelRank(days);

        // 1. 成功率偏低
        if (overview.getRequestCount() > 0 && overview.getSuccessRate().compareTo(BigDecimal.valueOf(90)) < 0) {
            suggestions.add(new AiCostSuggestionVO("warning", "调用成功率偏低",
                    "近期成功率为 " + overview.getSuccessRate() + "%, 失败调用同样可能产生成本, 建议排查错误原因并增加重试/降级策略。"));
        }

        // 2. 单一模型花费占比过高
        BigDecimal totalCost = overview.getTotalCost() == null ? BigDecimal.ZERO : overview.getTotalCost();
        if (totalCost.compareTo(BigDecimal.ZERO) > 0 && !models.isEmpty()) {
            AiModelRankVO top = models.stream()
                    .max((a, b) -> nz(a.getCost()).compareTo(nz(b.getCost())))
                    .orElse(null);
            if (top != null && nz(top.getCost()).compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = nz(top.getCost()).multiply(BigDecimal.valueOf(100))
                        .divide(totalCost, 1, RoundingMode.HALF_UP);
                if (ratio.compareTo(BigDecimal.valueOf(70)) >= 0) {
                    suggestions.add(new AiCostSuggestionVO("info", "成本集中于单一模型",
                            "模型「" + top.getModelName() + "」花费占比达 " + ratio + "%, 可评估在低复杂度场景改用更低价模型以降低成本。"));
                }
            }
        }

        // 3. 平均延迟过高
        if (overview.getAvgLatency() != null && overview.getAvgLatency() > 5000) {
            suggestions.add(new AiCostSuggestionVO("warning", "平均响应延迟较高",
                    "平均延迟为 " + Math.round(overview.getAvgLatency()) + " ms, 建议开启流式输出或上下文压缩以改善体验。"));
        }

        if (suggestions.isEmpty()) {
            suggestions.add(new AiCostSuggestionVO("info", "运行状况良好",
                    "当前统计范围内成本与性能指标正常, 暂无优化建议。"));
        }
        return suggestions;
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private LocalDateTime startTime(Integer days) {
        int d = (days == null || days <= 0) ? 30 : days;
        return LocalDateTime.now().minusDays(d);
    }

    /**
     * 将分组类型转换为 MySQL date_format 表达式
     */
    private String dateFormat(String groupType) {
        return switch (groupType == null ? "day" : groupType) {
            case "week" -> "%x-%v";
            case "month" -> "%Y-%m";
            default -> "%Y-%m-%d";
        };
    }
}
