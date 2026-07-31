package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.noear.solon.annotation.Component;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class WebSearchTool implements ToolExecutor {

    private static final String NAME = "web_search";
    private static final String DESCRIPTION = "通过网络搜索引擎搜索实时信息，获取最新的网页内容";

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0 Safari/537.36";

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
        int maxResults = args.containsKey("max_results") ? ((Number) args.get("max_results")).intValue() : 5;
        maxResults = Math.max(1, Math.min(maxResults, 10));

        try {
            return searchBing(query, maxResults);
        } catch (Exception e) {
            return "搜索失败: " + e.getMessage();
        }
    }

    /** 请求 Bing 搜索页并用 jsoup 解析结果条目（标题/链接/摘要） */
    private String searchBing(String query, int maxResults) {
        String url = "https://www.bing.com/search?q=" + HttpUtil.encodeParams(query, StandardCharsets.UTF_8);
        String html = HttpUtil.createGet(url)
                .header("User-Agent", USER_AGENT)
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(8000)
                .execute()
                .body();
        if (html == null || html.isBlank()) {
            return "搜索失败: 搜索引擎无响应";
        }

        Document doc = Jsoup.parse(html);
        Elements items = doc.select("li.b_algo");

        StringBuilder result = new StringBuilder();
        result.append("搜索结果: \"").append(query).append("\"\n\n");

        int count = 0;
        for (Element item : items) {
            Element titleLink = item.selectFirst("h2 a");
            if (titleLink == null) continue;
            String title = titleLink.text();
            String link = titleLink.attr("href");
            Element snippetEl = item.selectFirst(".b_caption p");
            String snippet = snippetEl != null ? snippetEl.text() : "";

            count++;
            result.append(count).append(". ").append(title).append("\n");
            if (!link.isBlank()) {
                result.append("   链接: ").append(link).append("\n");
            }
            if (!snippet.isBlank()) {
                result.append("   摘要: ").append(snippet).append("\n");
            }
            result.append("\n");
            if (count >= maxResults) break;
        }

        if (count == 0) {
            return "未找到与 \"" + query + "\" 相关的搜索结果，请更换关键词重试。";
        }
        return result.toString();
    }

    @Override
    public String getName() {
        return NAME;
    }
}
