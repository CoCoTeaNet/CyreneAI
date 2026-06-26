package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class ChatRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger conversationId;

    private BigInteger modelId;

    private List<ChatMessageDTO> messages;

    private Double temperature;

    private Double topP;

    private Integer maxTokens;

    private String systemPrompt;

    @Data
    @Accessors(chain = true)
    public static class ChatMessageDTO implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        private String role;

        private String content;
    }
}
