package net.cocotea.cyreneai.agent.tool;

import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import net.cocotea.cyreneai.service.rag.KnowledgeBaseService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class KnowledgeBaseTool implements ToolExecutor {

    private static final String NAME = "knowledge_base";
    private static final String DESCRIPTION = "从知识库中检索相关信息，用于回答基于私有文档的问题";

    @Inject
    private KnowledgeBaseService knowledgeBaseService;

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "kb_id", Map.of(
                                "type", "string",
                                "description", "知识库ID"
                        ),
                        "query", Map.of(
                                "type", "string",
                                "description", "检索查询内容"
                        ),
                        "top_k", Map.of(
                                "type", "integer",
                                "description", "返回结果数量(默认5)",
                                "default", 5
                        ),
                        "threshold", Map.of(
                                "type", "number",
                                "description", "相似度阈值(默认0.7)",
                                "default", 0.7
                        )
                ),
                "required", java.util.List.of("kb_id", "query")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        try {
            String kbId = (String) args.get("kb_id");
            String query = (String) args.get("query");
            int topK = args.containsKey("top_k") ? ((Number) args.get("top_k")).intValue() : 5;
            double threshold = args.containsKey("threshold") ? ((Number) args.get("threshold")).doubleValue() : 0.7;

            if (kbId == null || query == null) {
                return "错误: kb_id 和 query 参数不能为空";
            }

            var results = knowledgeBaseService.retrieve(new BigInteger(kbId), query, topK, threshold, "top_k");

            if (results == null || results.isEmpty()) {
                return "知识库检索未找到相关结果";
            }

            return results.stream()
                    .map(r -> {
                        String docInfo = r.getDocumentName() != null ? " [来源: " + r.getDocumentName() + "]" : "";
                        return "- " + r.getContent() + docInfo + " (相似度: " + r.getScore() + ")";
                    })
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "知识库检索错误: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
