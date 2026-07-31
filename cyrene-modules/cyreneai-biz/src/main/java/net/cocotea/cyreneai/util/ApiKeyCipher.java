package net.cocotea.cyreneai.util;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.noear.solon.Solon;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * API 密钥加解密与脱敏工具。
 * <p>
 * 存储在 {@code ai_model_provider.api_key} 中的密钥使用 AES-256-GCM（随机 IV）加密后，
 * 以 {@link #ENC_GCM_PREFIX} 前缀标记；历史数据兼容：
 * <ul>
 *     <li>{@link #ENC_PREFIX} 前缀 — 旧版 AES/ECB 密文，仅支持解密（不再产生新的 ECB 密文）</li>
 *     <li>无前缀 — 历史明文，{@link #decrypt} 原样返回，便于平滑迁移</li>
 * </ul>
 * 主密钥取自配置 {@code myapp.ai.api-key-secret}；未配置或仍为内置默认值时会输出告警，
 * 生产环境务必通过外部配置覆盖为强随机密钥。
 *
 * @author cyrene
 */
@Slf4j
public final class ApiKeyCipher {

    /** 旧版 AES/ECB 加密值前缀标记（仅兼容解密）。 */
    public static final String ENC_PREFIX = "ENC:";

    /** AES-256-GCM 加密值前缀标记，用于识别密文并保证幂等（避免重复加密）。 */
    public static final String ENC_GCM_PREFIX = "ENC2:";

    /** 主密钥配置项。 */
    private static final String SECRET_CFG_KEY = "myapp.ai.api-key-secret";

    /** 内置默认主密钥（源码公开，仅供开发环境兜底，生产必须通过配置覆盖）。 */
    private static final String DEFAULT_SECRET = "CyreneAI@ApiKey$Secret$V1";

    /** GCM 推荐 IV 长度：12 字节 */
    private static final int GCM_IV_LENGTH = 12;

    /** GCM 认证标签长度：128 位 */
    private static final int GCM_TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    /** 默认密钥告警只输出一次 */
    private static final AtomicBoolean DEFAULT_SECRET_WARNED = new AtomicBoolean(false);

    private ApiKeyCipher() {
    }

    /**
     * 加密明文密钥。null/空白原样返回；已加密（带前缀）原样返回，保证幂等。
     */
    public static String encrypt(String plain) {
        return encryptWith(plain, currentSecret());
    }

    /**
     * 使用指定主密钥加密（AES-256-GCM + 随机 IV），用于密钥轮换场景。
     */
    public static String encryptWith(String plain, String secret) {
        if (StrUtil.isBlank(plain) || isEncrypted(plain)) {
            return plain;
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            RANDOM.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, keyOf(secret), new GCMParameterSpec(GCM_TAG_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(cipherBytes, 0, out, iv.length, cipherBytes.length);
            return ENC_GCM_PREFIX + Base64.getEncoder().encodeToString(out);
        } catch (Exception e) {
            log.error("API 密钥加密失败", e);
            throw new BusinessException("API 密钥加密失败");
        }
    }

    /**
     * 解密密钥。null/空白原样返回；非密文（无前缀）视为历史明文原样返回。
     */
    public static String decrypt(String stored) {
        return decryptWith(stored, currentSecret());
    }

    /**
     * 使用指定主密钥解密，用于密钥轮换场景。同时兼容 GCM 新密文与旧版 ECB 密文。
     */
    public static String decryptWith(String stored, String secret) {
        if (StrUtil.isBlank(stored) || !isEncrypted(stored)) {
            return stored;
        }
        try {
            if (stored.startsWith(ENC_GCM_PREFIX)) {
                byte[] data = Base64.getDecoder().decode(stored.substring(ENC_GCM_PREFIX.length()));
                if (data.length <= GCM_IV_LENGTH) {
                    throw new IllegalArgumentException("密文长度非法");
                }
                Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
                GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, data, 0, GCM_IV_LENGTH);
                cipher.init(Cipher.DECRYPT_MODE, keyOf(secret), spec);
                byte[] plain = cipher.doFinal(data, GCM_IV_LENGTH, data.length - GCM_IV_LENGTH);
                return new String(plain, StandardCharsets.UTF_8);
            }
            // 旧版 AES/ECB 密文，仅兼容解密；重新保存时会以 GCM 重新加密
            String cipherText = stored.substring(ENC_PREFIX.length());
            return SecureUtil.aes(SecureUtil.sha256().digest(secret)).decryptStr(cipherText);
        } catch (Exception e) {
            log.error("API 密钥解密失败", e);
            throw new BusinessException("API 密钥解密失败，请检查主密钥配置是否变更");
        }
    }

    /**
     * 是否为已加密的值（新旧两种前缀均视为密文）。
     */
    public static boolean isEncrypted(String value) {
        return value != null && (value.startsWith(ENC_GCM_PREFIX) || value.startsWith(ENC_PREFIX));
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

    private static SecretKeySpec keyOf(String secret) {
        // 通过 SHA-256 派生 32 字节密钥，兼容任意长度的配置密钥（AES-256）
        return new SecretKeySpec(SecureUtil.sha256().digest(secret), "AES");
    }

    private static String currentSecret() {
        String secret = null;
        try {
            String cfg = Solon.cfg().get(SECRET_CFG_KEY);
            if (StrUtil.isNotBlank(cfg)) {
                secret = cfg;
            }
        } catch (Throwable ignore) {
            // Solon 未启动（如单测）时回退默认值
        }
        if (secret == null || DEFAULT_SECRET.equals(secret)) {
            if (DEFAULT_SECRET_WARNED.compareAndSet(false, true)) {
                log.warn("myapp.ai.api-key-secret 未配置或仍为内置默认值，密钥加密强度形同虚设，生产环境请务必覆盖为强随机密钥！");
            }
            return secret == null ? DEFAULT_SECRET : secret;
        }
        return secret;
    }
}
