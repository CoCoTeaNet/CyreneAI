package net.cocotea.cyreneai.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.agent.AgentService;
import net.cocotea.cyreneai.model.dto.AgentChatRequestDTO;
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

            agentService.chatStream(request, out);
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
}
