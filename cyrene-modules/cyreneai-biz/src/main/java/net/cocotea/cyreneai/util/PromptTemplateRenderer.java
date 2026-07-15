package net.cocotea.cyreneai.util;

import cn.hutool.core.util.StrUtil;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示词模板变量替换引擎。
 * 语法：{{variableName}}，未提供的变量保持原样并跳过。
 *
 * @author cyrene
 */
public final class PromptTemplateRenderer {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\{\\{\\s*([a-zA-Z_][a-zA-Z0-9_.]*)\\s*}}");

    private PromptTemplateRenderer() {
    }

    /**
     * 渲染模板，将 {{key}} 替换为 variables 中对应值；未匹配的变量原样保留。
     */
    public static String render(String template, Map<String, ?> variables) {
        if (StrUtil.isBlank(template)) {
            return template;
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        Matcher matcher = VAR_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder(template.length());
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            String replacement = value == null ? matcher.group(0) : String.valueOf(value);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
