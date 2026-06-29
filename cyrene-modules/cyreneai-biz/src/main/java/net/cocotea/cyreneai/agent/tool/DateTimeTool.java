package net.cocotea.cyreneai.agent.tool;

import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class DateTimeTool implements ToolExecutor {

    private static final String NAME = "datetime";
    private static final String DESCRIPTION = "获取当前日期、时间或时间戳，支持时区参数";

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "format", Map.of(
                                "type", "string",
                                "description", "输出格式: date(日期), time(时间), datetime(日期时间), timestamp(时间戳), weekday(星期几)",
                                "enum", java.util.List.of("date", "time", "datetime", "timestamp", "weekday")
                        ),
                        "timezone", Map.of(
                                "type", "string",
                                "description", "时区，例如 Asia/Shanghai, America/New_York (默认 Asia/Shanghai)"
                        )
                ),
                "required", java.util.List.of()
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String format = args != null ? (String) args.get("format") : null;
        String timezone = args != null ? (String) args.get("timezone") : null;

        ZoneId zone = timezone != null ? ZoneId.of(timezone) : ZoneId.of("Asia/Shanghai");
        LocalDateTime now = LocalDateTime.now(zone);

        if (format == null) format = "datetime";

        return switch (format) {
            case "date" -> "当前日期: " + now.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "time" -> "当前时间: " + now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "timestamp" -> "当前时间戳: " + System.currentTimeMillis();
            case "weekday" -> {
                String[] weekdays = {"星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日"};
                int dayOfWeek = now.getDayOfWeek().getValue() - 1;
                yield "今天是: " + weekdays[dayOfWeek];
            }
            default -> "当前日期时间: " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        };
    }

    @Override
    public String getName() {
        return NAME;
    }
}
