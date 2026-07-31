package net.cocotea.cyreneai.agent.tool;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
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
            // 外部接口字段可能缺失，逐层判空避免 NPE
            JSONObject currentCondition = firstObj(json.getJSONArray("current_condition"));
            if (currentCondition == null) {
                return "无法获取城市: " + city + " 的当前天气数据";
            }
            JSONObject nearestArea = firstObj(json.getJSONArray("nearest_area"));
            String areaName = city;
            if (nearestArea != null) {
                JSONObject areaNameObj = firstObj(nearestArea.getJSONArray("areaName"));
                if (areaNameObj != null && areaNameObj.getStr("value") != null) {
                    areaName = areaNameObj.getStr("value");
                }
            }

            StringBuilder result = new StringBuilder();
            result.append("🌍 城市: ").append(areaName).append("\n");
            result.append("🌡 温度: ").append(strOrDefault(currentCondition, "temp_C")).append("°C\n");
            result.append("☁ 天气: ").append(descValue(currentCondition.getJSONArray("weatherDesc"))).append("\n");
            result.append("💧 湿度: ").append(strOrDefault(currentCondition, "humidity")).append("%\n");
            result.append("💨 风速: ").append(strOrDefault(currentCondition, "windspeedKmph")).append(" km/h\n");
            result.append("👁 能见度: ").append(strOrDefault(currentCondition, "visibility")).append(" km\n");

            JSONArray forecasts = json.getJSONArray("weather");
            if (forecasts != null && !forecasts.isEmpty()) {
                result.append("\n📅 预报:\n");
                for (int i = 0; i < Math.min(3, forecasts.size()); i++) {
                    JSONObject day = forecasts.getJSONObject(i);
                    if (day == null) continue;
                    String desc = "";
                    JSONArray hourly = day.getJSONArray("hourly");
                    if (hourly != null && hourly.size() > 4) {
                        JSONObject noon = hourly.getJSONObject(4);
                        if (noon != null) {
                            desc = descValue(noon.getJSONArray("weatherDesc"));
                        }
                    }
                    result.append("  ").append(strOrDefault(day, "date"))
                            .append(": ").append(strOrDefault(day, "avgtempC")).append("°C")
                            .append(" (").append(desc).append(")\n");
                }
            }
            return result.toString();
        } catch (Exception e) {
            return "天气查询失败: " + e.getMessage() + "\n提示: 请确保城市名称正确，或使用英文名称尝试";
        }
    }

    /** 取 JSON 数组首个对象，数组为空或缺失时返回 null */
    private JSONObject firstObj(JSONArray array) {
        return array != null && !array.isEmpty() ? array.getJSONObject(0) : null;
    }

    /** 取字段值，缺失时返回占位符 */
    private String strOrDefault(JSONObject obj, String key) {
        String value = obj.getStr(key);
        return value != null ? value : "-";
    }

    /** 提取 weatherDesc 数组首个 value，缺失时返回占位符 */
    private String descValue(JSONArray weatherDesc) {
        JSONObject first = firstObj(weatherDesc);
        String value = first != null ? first.getStr("value") : null;
        return value != null ? value : "-";
    }

    @Override
    public String getName() {
        return NAME;
    }
}
