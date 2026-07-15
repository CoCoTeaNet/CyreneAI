package net.cocotea.cyreneai.service.governance.impl;

import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.service.RedisService;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.service.governance.RateLimitService;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;

import java.time.LocalDateTime;

/**
 * 基于 Redis 计数器的简易速率限制实现
 * <p>Key 结构:
 * <ul>
 *     <li>ai:rl:rpm:{keyId}:{yyyyMMddHHmm} — 分钟级请求数</li>
 *     <li>ai:rl:tpm:{keyId}:{yyyyMMddHHmm} — 分钟级 Token 数</li>
 * </ul>
 * 注意: 依赖 RedisService.get / save, 非原子, 在高并发下可能有少量超发, 属于可接受粒度.
 */
@Slf4j
@Component
public class RateLimitServiceImpl implements RateLimitService {

    private static final String KEY_RPM = "ai:rl:rpm:";
    private static final String KEY_TPM = "ai:rl:tpm:";
    private static final int TTL_SECONDS = 65;

    @Inject
    private RedisService redisService;

    @Override
    public Result check(AiApiKey key, int estimatedTokens) {
        if (key == null) return Result.pass();
        try {
            // 月度 Token 配额检查
            if (key.getMonthlyTokenQuota() != null && key.getMonthlyTokenQuota() > 0) {
                long used = key.getTokensUsedThisMonth() == null ? 0L : key.getTokensUsedThisMonth();
                if (used >= key.getMonthlyTokenQuota()) {
                    return Result.deny("quota_exceeded", "本月 Token 配额已用完");
                }
            }
            String bucket = currentBucket();
            // RPM
            if (key.getRpmLimit() != null && key.getRpmLimit() > 0) {
                long cur = readCounter(KEY_RPM + key.getId() + ":" + bucket);
                if (cur >= key.getRpmLimit()) {
                    return Result.deny("rate_limited", "请求频率超限(RPM=" + key.getRpmLimit() + ")");
                }
            }
            // TPM
            if (key.getTpmLimit() != null && key.getTpmLimit() > 0) {
                long cur = readCounter(KEY_TPM + key.getId() + ":" + bucket);
                if (cur + Math.max(estimatedTokens, 0) > key.getTpmLimit()) {
                    return Result.deny("rate_limited", "Token 频率超限(TPM=" + key.getTpmLimit() + ")");
                }
            }
        } catch (Exception e) {
            log.error("rate limit check failed", e);
            // 失败开放, 保证主链路可用
        }
        return Result.pass();
    }

    @Override
    public void increment(AiApiKey key, int tokens) {
        if (key == null) return;
        try {
            String bucket = currentBucket();
            addCounter(KEY_RPM + key.getId() + ":" + bucket, 1);
            if (tokens > 0) {
                addCounter(KEY_TPM + key.getId() + ":" + bucket, tokens);
            }
        } catch (Exception e) {
            log.error("rate limit increment failed", e);
        }
    }

    private String currentBucket() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d",
                now.getYear(), now.getMonthValue(), now.getDayOfMonth(),
                now.getHour(), now.getMinute());
    }

    private long readCounter(String key) {
        String v = redisService.get(key);
        if (v == null || v.isEmpty()) return 0L;
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void addCounter(String key, long delta) {
        long cur = readCounter(key);
        redisService.save(key, String.valueOf(cur + delta), (long) TTL_SECONDS);
    }
}
