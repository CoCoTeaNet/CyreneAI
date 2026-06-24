package net.cocotea.cyreneai.controller;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.validation.annotation.Valid;

@Slf4j
@Valid
@Controller
@Mapping("/ai/chat")
public class ChatController {

    @Get @Mapping("/ping")
    public String ping() {
        QwenChatModel chatModel = QwenChatModel.builder()
                .apiKey("")
                .modelName("qwen3.7-plus")
                .build();
        return chatModel.chat("Ping! ~~~///(^v^)\\\\~~~");
    }

}
