package net.cocotea.cyreneai.service.governance;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 内容安全检查结果
 */
@Data
@Accessors(chain = true)
public class ContentSafetyResult {

    /** 是否允许通过(false 表示拦截) */
    private boolean allowed = true;

    /** 命中动作;block, replace, warn, pass */
    private String action = "pass";

    /** 命中原因/规则名 */
    private String reason;

    /** 经过 replace 后的最终文本(若未替换则返回原文) */
    private String sanitizedText;

    public static ContentSafetyResult pass(String text) {
        return new ContentSafetyResult().setAllowed(true).setAction("pass").setSanitizedText(text);
    }

    public static ContentSafetyResult blocked(String reason) {
        return new ContentSafetyResult().setAllowed(false).setAction("block").setReason(reason);
    }

    public static ContentSafetyResult replaced(String sanitized, String reason) {
        return new ContentSafetyResult().setAllowed(true).setAction("replace").setSanitizedText(sanitized).setReason(reason);
    }

    public static ContentSafetyResult warned(String text, String reason) {
        return new ContentSafetyResult().setAllowed(true).setAction("warn").setSanitizedText(text).setReason(reason);
    }
}
