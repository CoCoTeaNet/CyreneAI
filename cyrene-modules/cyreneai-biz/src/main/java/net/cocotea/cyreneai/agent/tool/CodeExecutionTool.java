package net.cocotea.cyreneai.agent.tool;

import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import java.util.Map;

/**
 * 代码执行工具。
 * <p>
 * 安全说明：原实现通过 {@code ScriptEngine.eval(code)} 直接执行脚本，
 * 脚本可访问 Java 反射与 {@code Runtime.exec}，等价于任意命令执行（RCE），
 * 且无超时/资源限制。在引入真正的沙箱（如 GraalVM Polyglot
 * {@code allowAllAccess(false)} + 资源限制）之前，该工具已安全下线，
 * 执行时固定返回禁用提示。
 */
@Component
public class CodeExecutionTool implements ToolExecutor {

    private static final String NAME = "code_execution";
    private static final String DESCRIPTION = "执行简单的 JavaScript 代码片段（当前出于安全考虑已禁用）";

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
        // 安全下线：无沙箱的脚本执行等价于任意命令执行，禁止直接 eval 用户代码
        return "错误: 代码执行工具出于安全考虑已禁用。如需启用，请引入 GraalVM Polyglot 沙箱（allowAllAccess=false + 超时/资源限制）后重新实现本工具";
    }

    @Override
    public String getName() {
        return NAME;
    }
}
