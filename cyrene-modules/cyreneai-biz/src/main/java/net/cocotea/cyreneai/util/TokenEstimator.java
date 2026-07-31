package net.cocotea.cyreneai.util;

/**
 * Token 估算工具。
 * <p>
 * CJK 字符约 1.5 字符/token，其余约 4 字符/token；
 * 统一按 4 字符/token 会对中文严重低估，可能超出上下文窗口。
 *
 * @author cyrene
 */
public class TokenEstimator {

    private TokenEstimator() {
    }

    /**
     * 估算文本的 Token 数；null/空文本返回 0
     */
    public static int estimate(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int cjk = 0;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0x4E00 && c <= 0x9FFF) {
                cjk++;
            }
        }
        int other = text.length() - cjk;
        return (int) Math.ceil(cjk / 1.5 + other / 4.0);
    }
}
