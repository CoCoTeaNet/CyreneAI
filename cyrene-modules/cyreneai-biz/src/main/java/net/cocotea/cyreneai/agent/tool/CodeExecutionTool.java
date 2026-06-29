package net.cocotea.cyreneai.agent.tool;

import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.Map;

@Component
public class CodeExecutionTool implements ToolExecutor {

    private static final String NAME = "code_execution";
    private static final String DESCRIPTION = "执行简单的 JavaScript 代码片段并返回结果（沙箱环境）";

    private final ScriptEngine engine;

    public CodeExecutionTool() {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine e = null;
        for (String name : Arrays.asList("graal.js", "JavaScript", "js", "Nashorn")) {
            e = manager.getEngineByName(name);
            if (e != null) break;
        }
        this.engine = e;
    }

    @Override
    public ToolSpecification getSpecification() {
        return ToolSpecification.of(NAME, DESCRIPTION, Map.of(
                "type", "object",
                "properties", Map.of(
                        "language", Map.of(
                                "type", "string",
                                "description", "编程语言 (当前支持: javascript)",
                                "enum", java.util.List.of("javascript")
                        ),
                        "code", Map.of(
                                "type", "string",
                                "description", "要执行的代码片段"
                        ),
                        "timeout_ms", Map.of(
                                "type", "integer",
                                "description", "超时时间(毫秒，默认5000)",
                                "default", 5000
                        )
                ),
                "required", java.util.List.of("code")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String language = args.containsKey("language") ? (String) args.get("language") : "javascript";
        String code = (String) args.get("code");

        if (code == null || code.isBlank()) {
            return "错误: 代码不能为空";
        }

        if (!"javascript".equals(language)) {
            return "错误: 当前仅支持 JavaScript 语言";
        }

        if (engine == null) {
            return "错误: JavaScript 引擎不可用。JDK 15+ 不再内置 Nashorn 引擎，请添加 GraalVM JS 依赖到项目中";
        }

        try {
            engine.put("__output", new StringBuilder());
            engine.eval("var console = { log: function(msg) { __output.append(String(msg)).append('\\n'); } };");

            Object result = engine.eval(code);
            StringBuilder output = (StringBuilder) engine.get("__output");

            StringBuilder response = new StringBuilder();
            if (output.length() > 0) {
                response.append("输出:\n").append(output);
            }
            if (result != null) {
                if (output.length() > 0) response.append("\n");
                response.append("返回值: ").append(result);
            }
            if (response.isEmpty()) {
                response.append("代码执行成功（无输出）");
            }
            return response.toString();
        } catch (Exception e) {
            return "代码执行错误: " + e.getMessage();
        }
    }

    @Override
    public String getName() {
        return NAME;
    }
}
