package net.cocotea.cyreneai.agent;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import net.cocotea.cyreneai.model.po.AiTool;
import net.cocotea.cyreneai.util.SafeHttpUtils;
import org.noear.solon.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ToolExecutionService {

    private static final Logger log = LoggerFactory.getLogger(ToolExecutionService.class);

    /** 危险工具白名单：仅管理员角色可执行（无论直连端点还是 Agent 链路） */
    private static final Set<String> ADMIN_ONLY_TOOLS = Set.of("code_execution");

    private static final List<String> ADMIN_ROLES = List.of("role:super:admin", "role:simple:admin");

    private final Map<String, ToolExecutor> builtinExecutors = new ConcurrentHashMap<>();

    public ToolExecutionService() {
        autoRegisterBuiltinTools();
    }

    private void autoRegisterBuiltinTools() {
        try {
            var classes = List.of(
                    "net.cocotea.cyreneai.agent.tool.CalculatorTool",
                    "net.cocotea.cyreneai.agent.tool.DateTimeTool",
                    "net.cocotea.cyreneai.agent.tool.WebSearchTool",
                    "net.cocotea.cyreneai.agent.tool.KnowledgeBaseTool",
                    "net.cocotea.cyreneai.agent.tool.CodeExecutionTool",
                    "net.cocotea.cyreneai.agent.tool.ImageGenerationTool",
                    "net.cocotea.cyreneai.agent.tool.ImageRecognitionTool",
                    "net.cocotea.cyreneai.agent.tool.WeatherTool"
            );
            for (String className : classes) {
                try {
                    Class<?> clazz = Class.forName(className, true, getClass().getClassLoader());
                    ToolExecutor executor = (ToolExecutor) clazz.getDeclaredConstructor().newInstance();
                    registerBuiltin(executor);
                    log.info("Registered builtin tool: {}", executor.getName());
                } catch (ClassNotFoundException e) {
                    log.warn("Builtin tool class not found: {}", className);
                } catch (Exception e) {
                    log.error("Failed to register builtin tool: {}", className, e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to auto-register builtin tools", e);
        }
    }

    public void registerBuiltin(ToolExecutor executor) {
        builtinExecutors.put(executor.getName(), executor);
    }

    public ToolSpecification getSpecification(String name) {
        ToolExecutor executor = builtinExecutors.get(name);
        if (executor != null) {
            return executor.getSpecification();
        }
        return null;
    }

    public List<ToolSpecification> getAllSpecifications() {
        return builtinExecutors.values().stream()
                .map(ToolExecutor::getSpecification)
                .toList();
    }

    public List<ToolSpecification> getSpecifications(List<String> toolNames) {
        return toolNames.stream()
                .map(builtinExecutors::get)
                .filter(e -> e != null)
                .map(ToolExecutor::getSpecification)
                .toList();
    }

    public String executeBuiltin(String toolName, Map<String, Object> args) {
        if (ADMIN_ONLY_TOOLS.contains(toolName) && !hasAdminRole()) {
            log.warn("Blocked dangerous tool [{}] execution for non-admin user", toolName);
            return "错误: 工具 " + toolName + " 仅限管理员使用";
        }
        ToolExecutor executor = builtinExecutors.get(toolName);
        if (executor != null) {
            log.info("Executing builtin tool: {} with args: {}", toolName, args);
            return executor.execute(args);
        }
        return "错误: 未知的内置工具: " + toolName;
    }

    /**
     * 当前登录用户是否具备管理员角色；未登录或异常一律视为无权
     */
    private boolean hasAdminRole() {
        try {
            for (String role : ADMIN_ROLES) {
                if (StpUtil.hasRole(role)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.warn("check admin role failed: {}", e.getMessage());
        }
        return false;
    }

    public String executeCustom(AiTool tool, Map<String, Object> args) {
        try {
            String url = tool.getUrl();
            if (url == null || url.isBlank()) {
                return "错误: 自定义工具未配置URL";
            }
            // SSRF 防护：仅允许 http/https 且禁止内网/保留地址
            SafeHttpUtils.validateUrl(url);

            String method = tool.getHttpMethod() != null ? tool.getHttpMethod().toUpperCase() : "POST";
            String authType = tool.getAuthType() != null ? tool.getAuthType() : "none";
            String authValue = tool.getAuthValue();

            String body = args != null ? JSONUtil.toJsonStr(args) : "{}";

            HttpRequest request;
            if ("GET".equals(method)) {
                StringBuilder urlBuilder = new StringBuilder(url);
                if (args != null) {
                    for (var entry : args.entrySet()) {
                        String separator = urlBuilder.indexOf("?") > 0 ? "&" : "?";
                        urlBuilder.append(separator)
                                .append(SafeHttpUtils.encodeQueryParam(entry.getKey()))
                                .append("=")
                                .append(SafeHttpUtils.encodeQueryParam(String.valueOf(entry.getValue())));
                    }
                }
                request = HttpUtil.createGet(urlBuilder.toString());
            } else {
                request = HttpUtil.createPost(url).body(body);
            }

            request.timeout(10000);
            request.header("Content-Type", "application/json");

            if ("bearer".equalsIgnoreCase(authType) && authValue != null) {
                request.header("Authorization", "Bearer " + authValue);
            } else if ("basic".equalsIgnoreCase(authType) && authValue != null) {
                request.basicAuth(authValue, "");
            }

            String response = request.execute().body();
            log.info("Custom tool {} returned: {}", tool.getName(), response);
            return response;
        } catch (Exception e) {
            log.error("Custom tool execution failed: {}", tool.getName(), e);
            return "自定义工具执行失败: " + e.getMessage();
        }
    }

    public Map<String, Object> convertToolToMap(AiTool tool) {
        Map<String, Object> spec = new LinkedHashMap<>();
        spec.put("name", tool.getName());
        spec.put("description", tool.getDescription());
        if (tool.getSchemaJson() != null) {
            try {
                spec.put("parameters", JSONUtil.parse(tool.getSchemaJson()));
            } catch (Exception e) {
                spec.put("parameters", Map.of("type", "object", "properties", Map.of()));
            }
        } else {
            spec.put("parameters", Map.of("type", "object", "properties", Map.of()));
        }
        return spec;
    }
}
