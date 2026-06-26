package net.cocotea.cyreneai.service.impl;

import net.cocotea.cyreneai.model.po.AiConversation;
import net.cocotea.cyreneai.model.po.AiMessage;
import net.cocotea.cyreneai.service.AiConversationService;
import org.noear.solon.annotation.Component;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.EntityQuery;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class AiConversationServiceImpl implements AiConversationService {

    @Db
    private LightDao lightDao;

    @Override
    public AiConversation findById(BigInteger id) {
        return lightDao.load(new AiConversation(id));
    }

    @Override
    public List<AiConversation> listByUserId(BigInteger userId) {
        EntityQuery query = EntityQuery.create()
                .where("#[user_id = :userId]#[and is_deleted = :isDeleted]")
                .names("userId", "isDeleted")
                .values(userId, 0)
                .orderByDesc("updated_time");
        return lightDao.findEntity(AiConversation.class, query);
    }

    @Override
    public AiConversation create(AiConversation conversation) {
        conversation.setCreatedTime(LocalDateTime.now());
        conversation.setUpdatedTime(LocalDateTime.now());
        conversation.setIsDeleted(0);
        lightDao.save(conversation);
        return conversation;
    }

    @Override
    public void deleteById(BigInteger id) {
        AiConversation conv = new AiConversation();
        conv.setId(id);
        conv.setIsDeleted(1);
        lightDao.update(conv, "isDeleted");
    }

    @Override
    public List<AiMessage> listMessages(BigInteger conversationId) {
        EntityQuery query = EntityQuery.create()
                .where("#[conversation_id = :conversationId]")
                .names("conversationId")
                .values(conversationId)
                .orderBy("created_time");
        return lightDao.findEntity(AiMessage.class, query);
    }

    @Override
    public AiMessage saveMessage(AiMessage message) {
        message.setCreatedTime(LocalDateTime.now());
        lightDao.save(message);
        return message;
    }

    @Override
    public void deleteMessage(BigInteger id) {
        lightDao.delete(new AiMessage(id));
    }

    @Override
    public void clearMessages(BigInteger conversationId) {
        EntityQuery query = EntityQuery.create()
                .where("#[conversation_id = :conversationId]")
                .names("conversationId")
                .values(conversationId);
        lightDao.deleteByQuery(AiMessage.class, query);
    }
}
