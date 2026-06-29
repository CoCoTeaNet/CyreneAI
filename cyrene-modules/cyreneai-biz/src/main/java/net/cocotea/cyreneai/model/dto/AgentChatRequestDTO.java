package net.cocotea.cyreneai.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

@Data
@Accessors(chain = true)
public class AgentChatRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private BigInteger agentId;

    private BigInteger conversationId;

    private String message;

    private List<ChatRequestDTO.ChatMessageDTO> history;
}
