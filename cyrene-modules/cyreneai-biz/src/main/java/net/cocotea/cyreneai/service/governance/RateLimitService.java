package net.cocotea.cyreneai.service.governance;

import net.cocotea.cyreneai.model.po.AiApiKey;

/**
 * 速率与配额执行器
 */
public interface RateLimitService {

    /**
     * 校验 Key 的 RPM/TPM/月度 Token 配额是否允许放行
     * @param key   要校验的 Key(null 表示以登录用户凭证请求,不做 Key 级限制)
     * @param estimatedTokens 本次请求预估 Token(可传 0)
     */
    Result check(AiApiKey key, int estimatedTokens);

    /**
     * 请求成功后累加 Redis 计数
     */
    void increment(AiApiKey key, int tokens);

    /**
     * 校验结果
     * @param allowed  是否放行
     * @param reason   拒绝原因(rate_limited, quota_exceeded 等)
     * @param message  面向前端的可读描述
     */
    record Result(boolean allowed, String reason, String message) {
        public static Result pass() { return new Result(true, null, null); }
        public static Result deny(String reason, String message) { return new Result(false, reason, message); }
    }
}
