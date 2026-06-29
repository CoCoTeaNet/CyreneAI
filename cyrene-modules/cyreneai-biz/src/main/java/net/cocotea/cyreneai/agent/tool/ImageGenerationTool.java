package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.util.Map;

@Component
public class ImageGenerationTool implements ToolExecutor {

    private static final String NAME = "image_generation";
    private static final String DESCRIPTION = "根据文字描述生成图片，支持 DALL-E 3、Stable Diffusion 等模型（需要配置API密钥）";

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "prompt", Map.of(
                                "type", "string",
                                "description", "图片描述提示词（英文描述效果更佳）"
                        ),
                        "size", Map.of(
                                "type", "string",
                                "description", "图片尺寸: 1024x1024, 1024x1792, 1792x1024",
                                "default", "1024x1024"
                        ),
                        "n", Map.of(
                                "type", "integer",
                                "description", "生成数量(默认1)",
                                "default", 1
                        )
                ),
                "required", java.util.List.of("prompt")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String prompt = (String) args.get("prompt");
        if (prompt == null || prompt.isBlank()) {
            return "错误: 图片描述不能为空";
        }

        String size = args.containsKey("size") ? (String) args.get("size") : "1024x1024";
        int n = args.containsKey("n") ? ((Number) args.get("n")).intValue() : 1;

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "图片生成需要配置 OPENAI_API_KEY 环境变量。请先在系统环境变量中设置 OpenAI API Key。\n"
                    + "配置后支持的模型: DALL-E 3、Stable Diffusion 等";
        }

        try {
            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", "dall-e-3")
                    .set("prompt", prompt)
                    .set("n", n)
                    .set("size", size);

            String response = HttpUtil.createPost("https://api.openai.com/v1/images/generations")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(response);
            var data = json.getJSONArray("data");
            if (data != null && !data.isEmpty()) {
                StringBuilder result = new StringBuilder();
                result.append("✅ 图片生成成功!\n\n");
                for (int i = 0; i < data.size(); i++) {
                    var item = data.getJSONObject(i);
                    String url = item.getStr("url");
                    String revisedPrompt = item.getStr("revised_prompt");
                    result.append("图片 ").append(i + 1).append(": ").append(url).append("\n");
                    if (revisedPrompt != null) {
                        result.append("优化提示词: ").append(revisedPrompt).append("\n");
                    }
                    result.append("\n");
                }
                return result.toString();
            }
            return "图片生成返回异常: " + response;
        } catch (Exception e) {
            return "图片生成失败: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
