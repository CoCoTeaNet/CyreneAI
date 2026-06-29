package net.cocotea.cyreneai.agent.tool;

import net.cocotea.cyreneai.agent.ToolExecutor;
import net.cocotea.cyreneai.agent.ToolSpecification;
import org.noear.solon.annotation.Component;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;

@Component
public class CalculatorTool implements ToolExecutor {

    private static final String NAME = "calculator";
    private static final String DESCRIPTION = "执行数学表达式计算，支持 +, -, *, /, ^, sin, cos, tan, sqrt, log, abs, round, floor, ceil 等运算";

    private final ScriptEngine engine;

    public CalculatorTool() {
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
                        "expression", Map.of(
                                "type", "string",
                                "description", "要计算的数学表达式，例如: 2 + 3 * 4, sqrt(16), sin(0.5)"
                        )
                ),
                "required", java.util.List.of("expression")
        ));
    }

    @Override
    public String execute(Map<String, Object> args) {
        String expression = (String) args.get("expression");
        if (expression == null || expression.isBlank()) {
            return "错误: 表达式不能为空";
        }
        try {
            if (engine != null) {
                Object result = engine.eval(expression);
                return expression + " = " + result;
            }
            double result = eval(expression);
            if (result == Math.floor(result) && !Double.isInfinite(result)) {
                return expression + " = " + (long) result;
            }
            return expression + " = " + result;
        } catch (Exception e) {
            return "计算错误: " + e.getMessage() + "\n提示: JDK 15+ 不再内置 JavaScript 引擎，请添加 GraalVM JS 依赖以支持复杂表达式";
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    private static final Map<String, Double> CONSTANTS = Map.of("pi", Math.PI, "e", Math.E);
    private static final Map<String, DoubleUnaryOperator> FUNCTIONS = Map.of(
            "sin", Math::sin, "cos", Math::cos, "tan", Math::tan,
            "sqrt", Math::sqrt, "log", Math::log, "log10", Math::log10,
            "abs", Math::abs, "round", Math::rint, "floor", Math::floor, "ceil", Math::ceil
    );

    private static double eval(String expr) {
        return parseExpr(tokenize(expr));
    }

    private static Deque<String> tokenize(String expr) {
        Deque<String> tokens = new ArrayDeque<>();
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) continue;
            if (Character.isLetter(c) || c == '.') {
                buf.append(c);
            } else {
                if (!buf.isEmpty()) {
                    tokens.add(buf.toString());
                    buf.setLength(0);
                }
                if ("+-*/^()".indexOf(c) >= 0) {
                    tokens.add(String.valueOf(c));
                }
            }
        }
        if (!buf.isEmpty()) tokens.add(buf.toString());
        return tokens;
    }

    private static double parseExpr(Deque<String> tokens) {
        double result = parseTerm(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if ("+".equals(op)) {
                tokens.poll();
                result += parseTerm(tokens);
            } else if ("-".equals(op)) {
                tokens.poll();
                result -= parseTerm(tokens);
            } else {
                break;
            }
        }
        return result;
    }

    private static double parseTerm(Deque<String> tokens) {
        double result = parseFactor(tokens);
        while (!tokens.isEmpty()) {
            String op = tokens.peek();
            if ("*".equals(op)) {
                tokens.poll();
                result *= parseFactor(tokens);
            } else if ("/".equals(op)) {
                tokens.poll();
                double divisor = parseFactor(tokens);
                if (divisor == 0) throw new ArithmeticException("除以零");
                result /= divisor;
            } else if ("^".equals(op)) {
                tokens.poll();
                result = Math.pow(result, parseFactor(tokens));
            } else {
                break;
            }
        }
        return result;
    }

    private static double parseFactor(Deque<String> tokens) {
        if (tokens.isEmpty()) throw new IllegalArgumentException("表达式不完整");
        String token = tokens.poll();

        if (CONSTANTS.containsKey(token.toLowerCase())) {
            return CONSTANTS.get(token.toLowerCase());
        }

        if (FUNCTIONS.containsKey(token.toLowerCase())) {
            if (!"(".equals(tokens.peek())) {
                throw new IllegalArgumentException("函数 " + token + " 需要括号参数");
            }
            tokens.poll();
            double arg = parseExpr(tokens);
            if (!")".equals(tokens.poll())) {
                throw new IllegalArgumentException("缺少右括号");
            }
            return FUNCTIONS.get(token.toLowerCase()).applyAsDouble(arg);
        }

        if ("(".equals(token)) {
            double result = parseExpr(tokens);
            if (!")".equals(tokens.poll())) {
                throw new IllegalArgumentException("缺少右括号");
            }
            return result;
        }

        if ("-".equals(token)) {
            return -parseFactor(tokens);
        }
        if ("+".equals(token)) {
            return parseFactor(tokens);
        }

        try {
            return Double.parseDouble(token);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("无法解析: " + token);
        }
    }
}
