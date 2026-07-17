package net.cocotea.cyreneai.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.Solon;

/**
 * API 密钥加解密与脱敏工具。
 * <p>
 * 存储在 {@code ai_model_provider.api_key} 中的密钥使用 AES 加密后，以 {@link #ENC_PREFIX} 前缀标记。
 * 加解密均兼容历史明文：未带前缀的值视为明文，{@link #decrypt} 原样返回，便于平滑迁移。
 * 主密钥取自配置 {@code myapp.ai.api-key-secret}，未配置时使用内置默认值。
 *
 * @author cyrene
 */
@Slf4j
public final class ApiKeyCipher {

    /** 加密值前缀标记，用于识别密文并保证幂等（避免重复加密）。 */
    public static final String ENC_PREFIX = "ENC:";

    /** 主密钥配置项。 */
    private static final String SECRET_CFG_KEY = "myapp.ai.api-key-secret";

    /** 内置默认主密钥（建议通过配置覆盖）。 */
    private static final String DEFAULT_SECRET = "CyreneAI@ApiKey$Secret$V1";

    private ApiKeyCipher() {
    }

    /**
     * 加密明文密钥。null/空白原样返回；已加密（带前缀）原样返回，保证幂等。
     */
    public static String encrypt(String plain) {
        return encryptWith(plain, currentSecret());
    }

    /**
     * 使用指定主密钥加密，用于密钥轮换场景。
     */
    public static String encryptWith(String plain, String secret) {
        if (StrUtil.isBlank(plain) || isEncrypted(plain)) {
            return plain;
        }
        return ENC_PREFIX + aes(secret).encryptBase64(plain);
    }

    /**
     * 解密密钥。null/空白原样返回；非密文（无前缀）视为历史明文原样返回。
     */
    public static String decrypt(String stored) {
        return decryptWith(stored, currentSecret());
    }

    /**
     * 使用指定主密钥解密，用于密钥轮换场景。
     */
    public static String decryptWith(String stored, String secret) {
        if (StrUtil.isBlank(stored) || !isEncrypted(stored)) {
            return stored;
        }
        String cipher = stored.substring(ENC_PREFIX.length());
        return aes(secret).decryptStr(cipher);
    }

    /**
     * 是否为已加密的值。
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENC_PREFIX);
    }

    /**
     * 判断前端回传的值是否为脱敏占位（未被修改），此时不应覆盖原密钥。
     */
    public static boolean isMasked(String value) {
        return value != null && value.contains("****");
    }

    /**
     * 脱敏展示：保留前 3 位与后 4 位，如 {@code sk-****...ab12}。
     * 传入的应是解密后的明文。
     */
    public static String mask(String plain) {
        if (StrUtil.isBlank(plain)) {
            return plain;
        }
        if (plain.length() <= 8) {
            return "****";
        }
        String prefix = plain.substring(0, 3);
        String suffix = plain.substring(plain.length() - 4);
        return prefix + "****..." + suffix;
    }

    private static AES aes(String secret) {
        // 通过 SHA-256 派生 32 字节密钥，兼容任意长度的配置密钥（AES-256）
        byte[] key = SecureUtil.sha256().digest(secret);
        return SecureUtil.aes(key);
    }

    private static String currentSecret() {
        try {
            String cfg = Solon.cfg().get(SECRET_CFG_KEY);
            if (StrUtil.isNotBlank(cfg)) {
                return cfg;
            }
        } catch (Throwable ignore) {
            // Solon 未启动（如单测）时回退默认值
        }
        return DEFAULT_SECRET;
    }
}
