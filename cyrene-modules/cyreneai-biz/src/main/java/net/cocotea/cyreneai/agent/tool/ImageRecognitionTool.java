package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.util.Base64;
import java.util.Map;

@Component
public class ImageRecognitionTool implements ToolExecutor {

    private static final String NAME = "image_recognition";
    private static final String DESCRIPTION = "识别图片内容，支持读取图片中的文字、物体、场景等信息（需要配置多模态模型API密钥）";

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "image_url", Map.of(
                                "type", "string",
                                "description", "图片的URL地址"
                        ),
                        "question", Map.of(
                                "type", "string",
                                "description", "关于图片的问题，例如: 图片里有什么？请识别图中的文字"
                        ),
                        "detail", Map.of(
                                "type", "string",
                                "description", "识别详细程度: auto, low, high",
                                "default", "auto"
                        )
                ),
                "required", java.util.List.of("image_url", "question")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String imageUrl = (String) args.get("image_url");
        String question = (String) args.get("question");

        if (imageUrl == null || imageUrl.isBlank()) {
            return "错误: 图片URL不能为空";
        }
        if (question == null || question.isBlank()) {
            question = "请详细描述这张图片的内容";
        }

        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            return "图片识别需要配置 OPENAI_API_KEY 环境变量。\n"
                    + "支持的模型: GPT-4V, GPT-4o, Qwen-VL, Claude 3 等多模态模型";
        }

        try {
            JSONArray messages = new JSONArray();
            JSONObject userMsg = new JSONObject();
            userMsg.set("role", "user");
            JSONArray contentArray = new JSONArray();
            contentArray.add(JSONUtil.createObj()
                    .set("type", "text")
                    .set("text", question));
            contentArray.add(JSONUtil.createObj()
                    .set("type", "image_url")
                    .set("image_url", JSONUtil.createObj()
                            .set("url", imageUrl)
                            .set("detail", args.getOrDefault("detail", "auto"))));
            userMsg.set("content", contentArray);
            messages.add(userMsg);

            JSONObject requestBody = JSONUtil.createObj()
                    .set("model", "gpt-4o")
                    .set("messages", messages)
                    .set("max_tokens", 1000);

            String response = HttpUtil.createPost("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toString())
                    .timeout(30000)
                    .execute()
                    .body();

            JSONObject json = JSONUtil.parseObj(response);
            var choices = json.getJSONArray("choices");
            if (choices != null && !choices.isEmpty()) {
                String content_ = choices.getJSONObject(0)
                        .getJSONObject("message")
                        .getStr("content");
                return "图片识别结果:\n" + content_;
            }
            return "图片识别返回异常: " + response;
        } catch (Exception e) {
            return "图片识别失败: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
