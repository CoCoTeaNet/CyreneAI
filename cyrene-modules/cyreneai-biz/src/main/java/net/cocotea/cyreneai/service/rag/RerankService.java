package net.cocotea.cyreneai.service.rag;

import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;

import java.util.List;

/**
 * Rerank 重排序服务。
 * <p>
 * 在向量检索得到候选片段后，调用重排序模型（如 DashScope gte-rerank / Cohere rerank）
 * 根据 query 与片段的语义相关性重新打分排序，返回更精准的 top-N 结果。
 * 未配置重排序模型时应优雅降级，按原顺序截取 top-N。
 */
public interface RerankService {

    /**
     * 是否存在可用的重排序模型配置。
     */
    boolean isAvailable();

    /**
     * 对候选检索结果进行重排序。
     *
     * @param query      查询文本
     * @param candidates 向量检索得到的候选片段
     * @param topN       返回条数
     * @return 重排序后的 top-N 结果（含更新后的 score）
     */
    List<AiRetrievalResultVO> rerank(String query, List<AiRetrievalResultVO> candidates, int topN);
}
