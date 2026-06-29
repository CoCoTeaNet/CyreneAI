package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class WeatherTool implements ToolExecutor {

    private static final String NAME = "weather";
    private static final String DESCRIPTION = "查询指定城市的当前天气和天气预报信息";

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "city", Map.of(
                                "type", "string",
                                "description", "城市名称，例如: 北京, 上海, 东京, London"
                        ),
                        "units", Map.of(
                                "type", "string",
                                "description", "温度单位: metric(摄氏度) 或 imperial(华氏度)",
                                "enum", java.util.List.of("metric", "imperial"),
                                "default", "metric"
                        )
                ),
                "required", java.util.List.of("city")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String city = (String) args.get("city");
        if (city == null || city.isBlank()) {
            return "错误: 城市名称不能为空";
        }
        try {
            String url = "https://wttr.in/" + URLEncoder.encode(city, StandardCharsets.UTF_8) + "?format=j1&lang=zh";
            String response = HttpUtil.get(url, 5000);
            if (response == null || response.isBlank()) {
                return "无法获取城市: " + city + " 的天气信息";
            }
            JSONObject json = JSONUtil.parseObj(response);
            var currentCondition = json.getJSONArray("current_condition").getJSONObject(0);
            var nearestArea = json.getJSONArray("nearest_area").getJSONObject(0);
            String areaName = nearestArea.getJSONArray("areaName").getJSONObject(0).getStr("value");

            StringBuilder result = new StringBuilder();
            result.append("🌍 城市: ").append(areaName).append("\n");
            result.append("🌡 温度: ").append(currentCondition.getStr("temp_C")).append("°C\n");
            result.append("☁ 天气: ").append(currentCondition.getJSONArray("weatherDesc").getJSONObject(0).getStr("value")).append("\n");
            result.append("💧 湿度: ").append(currentCondition.getStr("humidity")).append("%\n");
            result.append("💨 风速: ").append(currentCondition.getStr("windspeedKmph")).append(" km/h\n");
            result.append("👁 能见度: ").append(currentCondition.getStr("visibility")).append(" km\n");

            var forecasts = json.getJSONArray("weather");
            if (forecasts != null && !forecasts.isEmpty()) {
                result.append("\n📅 预报:\n");
                for (int i = 0; i < Math.min(3, forecasts.size()); i++) {
                    var day = forecasts.getJSONObject(i);
                    result.append("  ").append(day.getStr("date"))
                            .append(": ").append(day.getStr("avgtempC")).append("°C")
                            .append(" (").append(day.getJSONArray("hourly").getJSONObject(4)
                                    .getJSONArray("weatherDesc").getJSONObject(0).getStr("value"))
                            .append(")\n");
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "天气查询失败: " + e.getMessage() + "\n提示: 请确保城市名称正确，或使用英文名称尝试";
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
