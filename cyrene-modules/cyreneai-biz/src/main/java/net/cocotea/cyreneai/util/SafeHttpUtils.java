package net.cocotea.cyreneai.util;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import net.cocotea.cyreneadmin.model.BusinessException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.IDN;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 外部 HTTP 访问安全工具：SSRF 防护 + 响应大小限制。
 * <p>
 * 用于所有"用户可控 URL"的出站请求（网页抓取、音频下载、自定义工具调用等）：
 * <ul>
 *     <li>{@link #validateUrl} — 仅允许 http/https，且解析后的目标 IP 不得为环回/内网/链路本地等保留地址</li>
 *     <li>{@link #downloadBytes} — 带大小上限的下载，禁止重定向（防止校验后跳转到内网）</li>
 *     <li>{@link #encodeQueryParam} — GET 查询参数 URL 编码</li>
 * </ul>
 *
 * @author cyrene
 */
public final class SafeHttpUtils {

    /** 默认下载大小上限：20MB */
    public static final long DEFAULT_MAX_BYTES = 20L * 1024 * 1024;

    private SafeHttpUtils() {
    }

    /**
     * 校验用户提供的外部 URL，非法时抛出 {@link BusinessException}
     */
    public static void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new BusinessException("URL 不能为空");
        }
        URI uri;
        try {
            uri = URI.create(url.trim());
        } catch (Exception e) {
            throw new BusinessException("URL 格式非法");
        }
        String scheme = uri.getScheme();
        if (scheme == null || !("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))) {
            throw new BusinessException("仅支持 http/https 协议");
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new BusinessException("URL 缺少主机名");
        }
        InetAddress[] addresses;
        try {
            addresses = InetAddress.getAllByName(IDN.toASCII(host));
        } catch (Exception e) {
            throw new BusinessException("无法解析主机名: " + host);
        }
        for (InetAddress addr : addresses) {
            if (isBlockedAddress(addr)) {
                throw new BusinessException("禁止访问内网/保留地址: " + host);
            }
        }
    }

    /**
     * 带大小上限的安全下载。先校验 URL，再以流式读取并限制总量；禁止重定向。
     *
     * @param url      外部资源地址
     * @param maxBytes 最大字节数，超过则抛出业务异常
     */
    public static byte[] downloadBytes(String url, long maxBytes) {
        validateUrl(url);
        try (HttpResponse response = HttpUtil.createGet(url)
                .setFollowRedirects(false)
                .timeout(15000)
                .executeAsync()) {
            if (!response.isOk()) {
                throw new BusinessException("下载失败, HTTP状态码: " + response.getStatus());
            }
            long contentLength = response.contentLength();
            if (contentLength > maxBytes) {
                throw new BusinessException("资源大小超过限制: " + contentLength + " > " + maxBytes);
            }
            try (InputStream in = response.bodyStream();
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                long total = 0;
                int n;
                while ((n = in.read(buffer)) != -1) {
                    total += n;
                    if (total > maxBytes) {
                        throw new BusinessException("资源大小超过限制: " + maxBytes + " 字节");
                    }
                    out.write(buffer, 0, n);
                }
                return out.toByteArray();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("下载失败: " + e.getMessage());
        }
    }

    /**
     * 按默认上限 {@link #DEFAULT_MAX_BYTES} 下载
     */
    public static byte[] downloadBytes(String url) {
        return downloadBytes(url, DEFAULT_MAX_BYTES);
    }

    /**
     * GET 查询参数 URL 编码
     */
    public static String encodeQueryParam(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    /**
     * 是否为禁止访问的地址：环回 / 任意 / 链路本地 / 站点本地(私网) / 组播，
     * 以及 IPv4 保留段（100.64/10 CGNAT、169.254 云元数据等已被上述规则覆盖的除外）
     */
    private static boolean isBlockedAddress(InetAddress addr) {
        if (addr.isLoopbackAddress() || addr.isAnyLocalAddress() || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress() || addr.isMulticastAddress()) {
            return true;
        }
        byte[] ip = addr.getAddress();
        if (ip.length == 4) {
            int first = ip[0] & 0xFF;
            int second = ip[1] & 0xFF;
            // 100.64.0.0/10 (CGNAT)
            if (first == 100 && second >= 64 && second <= 127) {
                return true;
            }
            // 192.0.0.0/24 (IETF 保留)
            if (first == 192 && second == 0 && (ip[2] & 0xFF) == 0) {
                return true;
            }
            // 198.18.0.0/15 (基准测试)
            return first == 198 && (second == 18 || second == 19);
        }
        return false;
    }
}
