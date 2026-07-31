package net.cocotea.cyreneai.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.agent.AgentService;
import net.cocotea.cyreneai.model.dto.AgentChatRequestDTO;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.service.AiApiKeyService;
import net.cocotea.cyreneai.service.governance.RateLimitService;
import net.cocotea.cyreneai.util.TokenEstimator;
import org.noear.solon.annotation.*;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Slf4j
@Valid
@Controller
@Mapping("/ai/agent")
public class AgentChatController {

    @Inject
    private AgentService agentService;

    @Inject
    private AiApiKeyService aiApiKeyService;

    @Inject
    private RateLimitService rateLimitService;

    /** 平台 API Key 明文前缀 */
    private static final String API_KEY_PREFIX = "sk-cyr-";

    @Post
    @Mapping("/chat")
    public void chat(@Body AgentChatRequestDTO request) throws IOException {
        Context ctx = Context.current();
        ctx.contentType("text/event-stream;charset=utf-8");
        ctx.headerAdd("Cache-Control", "no-cache");
        ctx.headerAdd("Connection", "keep-alive");
        ctx.headerAdd("X-Accel-Buffering", "no");

        OutputStream out = ctx.outputStream();

        try {
            if (request.getAgentId() == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "请指定智能体ID")));
                out.flush();
                writeSseData(out, "[DONE]");
                out.flush();
                return;
            }

            if (request.getMessage() == null || request.getMessage().isBlank()) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "请输入消息内容")));
                out.flush();
                writeSseData(out, "[DONE]");
                out.flush();
                return;
            }

            // 限流/配额：携带平台 API Key（sk-cyr-）时执行 Key 级 RPM/TPM/月度 Token 配额校验
            AiApiKey apiKey = resolveApiKey(ctx);
            int estimatedTokens = TokenEstimator.estimate(request.getMessage());
            RateLimitService.Result limit = rateLimitService.check(apiKey, estimatedTokens);
            if (!limit.allowed()) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", limit.message())));
                out.flush();
                writeSseData(out, "[DONE]");
                out.flush();
                if (apiKey != null) {
                    aiApiKeyService.recordUsage(apiKey.getId(), apiKey.getUserId(), 0, 0, null, limit.reason());
                }
                return;
            }

            agentService.chatStream(request, out);

            // Key 级限流计数与用量落库（agent 链路无法获取真实用量，按输入估算计入）
            if (apiKey != null) {
                rateLimitService.increment(apiKey, estimatedTokens);
                aiApiKeyService.recordUsage(apiKey.getId(), apiKey.getUserId(), estimatedTokens, 0, null, "success");
            }
        } catch (Exception e) {
            log.error("Agent chat error", e);
            try {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", e.getMessage())));
                out.flush();
                writeSseData(out, "[DONE]");
                out.flush();
            } catch (IOException ex) {
                log.error("SSE write error", ex);
            }
        }
    }

    private void writeSseData(OutputStream out, String data) throws IOException {
        String event = "data: " + data + "\n\n";
        out.write(event.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * 从请求头解析平台 API Key（Authorization: Bearer sk-cyr-xxx 或 X-Api-Key）；
     * 非平台 Key 前缀（如登录用户的会话 Token）返回 null，不做 Key 级限流
     */
    private AiApiKey resolveApiKey(Context ctx) {
        try {
            String raw = ctx.header("Authorization");
            if (raw != null && raw.startsWith("Bearer ")) {
                raw = raw.substring(7).trim();
            }
            if (raw == null || !raw.startsWith(API_KEY_PREFIX)) {
                raw = ctx.header("X-Api-Key");
            }
            if (raw == null || !raw.startsWith(API_KEY_PREFIX)) {
                return null;
            }
            return aiApiKeyService.verifyPlainKey(raw);
        } catch (Exception e) {
            log.warn("resolve api key failed", e);
            return null;
        }
    }
}
