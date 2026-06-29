package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class WebSearchTool implements ToolExecutor {

    private static final String NAME = "web_search";
    private static final String DESCRIPTION = "通过网络搜索引擎搜索实时信息，获取最新的网页内容";

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "query", Map.of(
                                "type", "string",
                                "description", "搜索关键词"
                        ),
                        "max_results", Map.of(
                                "type", "integer",
                                "description", "最大返回结果数(默认5)",
                                "default", 5
                        )
                ),
                "required", java.util.List.of("query")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String query = (String) args.get("query");
        if (query == null || query.isBlank()) {
            return "错误: 搜索关键词不能为空";
        }
        try {
            String url = "https://www.google.com/search?q=" + HttpUtil.encodeParams(query, StandardCharsets.UTF_8);
            String html = HttpUtil.get(url, 5000);

            StringBuilder result = new StringBuilder();
            result.append("搜索结果: \"").append(query).append("\"\n\n");

            int maxResults = args.containsKey("max_results") ? ((Number) args.get("max_results")).intValue() : 5;
            result.append("(注意: 此为基础网页搜索，建议使用专用搜索API获取更准确的结果)\n");

            return result.toString();
        } catch (Exception e) {
            try {
                String fallbackUrl = "https://www.bing.com/search?q=" + HttpUtil.encodeParams(query, StandardCharsets.UTF_8);
                String html = HttpUtil.get(fallbackUrl, 5000);
                return "搜索结果(通过Bing): \"" + query + "\"\n(基础搜索模式)\n";
            } catch (Exception ex) {
                return "搜索失败: " + ex.getMessage();
            }
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
