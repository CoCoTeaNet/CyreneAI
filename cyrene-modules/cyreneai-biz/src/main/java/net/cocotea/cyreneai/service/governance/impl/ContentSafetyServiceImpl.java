package net.cocotea.cyreneai.service.governance.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.po.AiModerationRule;
import net.cocotea.cyreneai.model.po.AiSensitiveWord;
import net.cocotea.cyreneai.service.AiModerationRuleService;
import net.cocotea.cyreneai.service.AiSensitiveWordService;
import net.cocotea.cyreneai.service.governance.ContentSafetyResult;
import net.cocotea.cyreneai.service.governance.ContentSafetyService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 内容安全检查实现
 * <p>顺序: 敏感词表 → 审核规则(按 sort 降序)
 * <p>动作优先级: block > replace > warn > pass
 */
@Slf4j
@Component
public class ContentSafetyServiceImpl implements ContentSafetyService {

    @Inject
    private AiSensitiveWordService sensitiveWordService;

    @Inject
    private AiModerationRuleService moderationRuleService;

    @Override
    public ContentSafetyResult check(String text, String target) {
        if (StrUtil.isBlank(text)) return ContentSafetyResult.pass(text);
        if (target == null) target = "input";
        String working = text;
        String warnReason = null;

        // 1. 敏感词
        try {
            List<AiSensitiveWord> words = sensitiveWordService.listEnabled();
            for (AiSensitiveWord w : words) {
                if (!matchTarget(w.getTarget(), target)) continue;
                if (StrUtil.isBlank(w.getWord()) || !working.contains(w.getWord())) continue;
                String strategy = w.getStrategy();
                if ("block".equalsIgnoreCase(strategy)) {
                    return ContentSafetyResult.blocked("sensitive_word:" + w.getWord());
                } else if ("replace".equalsIgnoreCase(strategy)) {
                    String rep = StrUtil.isBlank(w.getReplacement()) ? "***" : w.getReplacement();
                    working = working.replace(w.getWord(), rep);
                } else if ("warn".equalsIgnoreCase(strategy)) {
                    warnReason = "sensitive_word:" + w.getWord();
                }
            }
        } catch (Exception e) {
            log.error("sensitive word check failed", e);
        }

        // 2. 审核规则
        try {
            List<AiModerationRule> rules = moderationRuleService.listEnabled();
            for (AiModerationRule r : rules) {
                if (!matchTarget(r.getTarget(), target)) continue;
                String provider = r.getProvider();
                boolean hit = false;
                String hitReason = null;
                if ("keyword_regex".equalsIgnoreCase(provider) && StrUtil.isNotBlank(r.getConfigJson())) {
                    try {
                        Pattern p = Pattern.compile(r.getConfigJson());
                        if (p.matcher(working).find()) {
                            hit = true;
                            hitReason = "regex:" + r.getName();
                        }
                    } catch (PatternSyntaxException pex) {
                        log.warn("invalid moderation regex pattern in rule {}", r.getId());
                    }
                } else if ("sensitive_word".equalsIgnoreCase(provider) && StrUtil.isNotBlank(r.getConfigJson())) {
                    for (String w : r.getConfigJson().split(",")) {
                        if (StrUtil.isNotBlank(w) && working.contains(w.trim())) {
                            hit = true;
                            hitReason = "moderation_word:" + w.trim();
                            break;
                        }
                    }
                } else if ("openai_moderation".equalsIgnoreCase(provider) || "dashscope".equalsIgnoreCase(provider)) {
                    // TODO: 接入 OpenAI / DashScope Moderation API; 当前版本仅记录规则未实际调用
                    log.debug("external moderation provider [{}] not implemented yet", provider);
                }
                if (hit) {
                    String action = r.getAction() == null ? "block" : r.getAction();
                    switch (action.toLowerCase()) {
                        case "block" -> { return ContentSafetyResult.blocked(hitReason); }
                        case "replace" -> working = "***";
                        case "warn" -> warnReason = hitReason;
                        default -> { /* pass */ }
                    }
                }
            }
        } catch (Exception e) {
            log.error("moderation rule check failed", e);
        }

        if (!working.equals(text)) {
            return ContentSafetyResult.replaced(working, "replaced");
        }
        if (warnReason != null) {
            return ContentSafetyResult.warned(working, warnReason);
        }
        return ContentSafetyResult.pass(working);
    }

    private boolean matchTarget(String ruleTarget, String requestTarget) {
        if (StrUtil.isBlank(ruleTarget) || "both".equalsIgnoreCase(ruleTarget)) return true;
        return ruleTarget.equalsIgnoreCase(requestTarget);
    }
}
