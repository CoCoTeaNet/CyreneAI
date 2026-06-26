package net.cocotea.cyreneai.service;

import net.cocotea.cyreneai.model.po.AiConversation;
import net.cocotea.cyreneai.model.po.AiMessage;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

public interface AiConversationService {

    AiConversation findById(BigInteger id);

    List<AiConversation> listByUserId(BigInteger userId);

    AiConversation create(AiConversation conversation);

    void deleteById(BigInteger id);

    List<AiMessage> listMessages(BigInteger conversationId);

    AiMessage saveMessage(AiMessage message);

    void deleteMessage(BigInteger id);

    void clearMessages(BigInteger conversationId);

    void truncateMessages(BigInteger conversationId, BigInteger afterMessageId);

    AiMessage updateMessage(AiMessage message);

    void updateTitle(BigInteger id, String title);

    String shareConversation(BigInteger id);

    Map<String, Object> getSharedConversation(String token);
}
