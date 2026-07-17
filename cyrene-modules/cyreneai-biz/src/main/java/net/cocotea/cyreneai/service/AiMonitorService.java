package net.cocotea.cyreneai.service;

import net.cocotea.cyreneai.model.vo.AiCostStatVO;
import net.cocotea.cyreneai.model.vo.AiCostSuggestionVO;
import net.cocotea.cyreneai.model.vo.AiModelRankVO;
import net.cocotea.cyreneai.model.vo.AiMonitorOverviewVO;
import net.cocotea.cyreneai.model.vo.AiTokenTrendVO;
import net.cocotea.cyreneai.model.vo.AiUserRankVO;

import java.util.List;

/**
 * AI 监控与观测: 基于 ai_audit_log 聚合统计
 */
public interface AiMonitorService {

    /**
     * 总览统计(近 days 天)
     */
    AiMonitorOverviewVO overview(Integer days);

    /**
     * Token / 花费 趋势
     * @param groupType day / week / month
     * @param days      统计范围天数
     */
    List<AiTokenTrendVO> tokenTrend(String groupType, Integer days);

    /**
     * 模型调用排行 / 平均延迟
     */
    List<AiModelRankVO> modelRank(Integer days);

    /**
     * 用户调用排行
     */
    List<AiUserRankVO> userRank(Integer days);

    /**
     * 成本统计(按 model / user / time 维度)
     */
    List<AiCostStatVO> costStat(String dimension, Integer days);

    /**
     * 成本优化建议
     */
    List<AiCostSuggestionVO> costSuggestions(Integer days);
}
