package net.cocotea.cyreneai.service;

import net.cocotea.cyreneai.model.po.AiConversation;
import net.cocotea.cyreneai.model.po.AiMessage;

import java.math.BigInteger;
import java.util.List;

public interface AiConversationService {

    AiConversation findById(BigInteger id);

    List<AiConversation> listByUserId(BigInteger userId);

    AiConversation create(AiConversation conversation);

    void deleteById(BigInteger id);

    List<AiMessage> listMessages(BigInteger conversationId);

    AiMessage saveMessage(AiMessage message);

    void deleteMessage(BigInteger id);

    void clearMessages(BigInteger conversationId);
}
