package net.cocotea.cyreneai.controller;

import cn.dev33.satoken.stp.StpUtil;
import net.cocotea.cyreneai.model.po.AiConversation;
import net.cocotea.cyreneai.model.po.AiMessage;
import net.cocotea.cyreneai.service.AiConversationService;
import net.cocotea.cyreneadmin.model.ApiResult;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Param;
import org.noear.solon.validation.annotation.Valid;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Valid
@Controller
@Mapping("/ai/conversation")
public class ConversationController {

    @Inject
    private AiConversationService conversationService;

    @Get @Mapping("/list")
    public ApiResult<List<AiConversation>> list() {
        BigInteger userId = BigInteger.valueOf(StpUtil.getLoginIdAsLong());
        List<AiConversation> conversations = conversationService.listByUserId(userId);
        return ApiResult.ok(conversations);
    }

    @Post @Mapping("/create")
    public ApiResult<AiConversation> create(@Body AiConversation conversation) {
        BigInteger userId = BigInteger.valueOf(StpUtil.getLoginIdAsLong());
        conversation.setUserId(userId);
        AiConversation created = conversationService.create(conversation);
        return ApiResult.ok(created);
    }

    @Post @Mapping("/delete/{id}")
    public ApiResult<?> delete(@Param("id") BigInteger id) {
        conversationService.deleteById(id);
        return ApiResult.ok();
    }

    @Get @Mapping("/messages")
    public ApiResult<List<AiMessage>> messages(@Param("conversationId") BigInteger conversationId) {
        List<AiMessage> messages = conversationService.listMessages(conversationId);
        return ApiResult.ok(messages);
    }

    @Post @Mapping("/saveMessage")
    public ApiResult<AiMessage> saveMessage(@Body AiMessage message) {
        AiMessage saved = conversationService.saveMessage(message);
        return ApiResult.ok(saved);
    }

    @Post @Mapping("/deleteMessage/{id}")
    public ApiResult<?> deleteMessage(@Param("id") BigInteger id) {
        conversationService.deleteMessage(id);
        return ApiResult.ok();
    }

    @Post @Mapping("/clearMessages/{conversationId}")
    public ApiResult<?> clearMessages(@Param("conversationId") BigInteger conversationId) {
        conversationService.clearMessages(conversationId);
        return ApiResult.ok();
    }

    @Post @Mapping("/share/{conversationId}")
    public ApiResult<Map<String, String>> share(@Param("conversationId") BigInteger conversationId) {
        String token = conversationService.shareConversation(conversationId);
        return ApiResult.ok(Map.of("token", token, "url", "/api/ai/conversation/shared/" + token));
    }

    @Get @Mapping("/shared/{token}")
    public ApiResult<?> getShared(@Param("token") String token) {
        Map<String, Object> data = conversationService.getSharedConversation(token);
        if (data == null) {
            return ApiResult.error("Share link expired or invalid");
        }
        return ApiResult.ok(data);
    }

    @Post @Mapping("/truncateMessages/{conversationId}/{afterMessageId}")
    public ApiResult<?> truncateMessages(@Param("conversationId") BigInteger conversationId,
                                         @Param("afterMessageId") BigInteger afterMessageId) {
        conversationService.truncateMessages(conversationId, afterMessageId);
        return ApiResult.ok();
    }

    @Get @Mapping("/export")
    public ApiResult<?> export(@Param("conversationId") BigInteger conversationId) {
        AiConversation conversation = conversationService.findById(conversationId);
        if (conversation == null) {
            return ApiResult.error("Conversation not found");
        }
        List<AiMessage> messages = conversationService.listMessages(conversationId);
        Map<String, Object> exportData = Map.of(
                "conversation", conversation,
                "messages", messages
        );
        return ApiResult.ok(exportData);
    }

    @Post @Mapping("/import")
    @SuppressWarnings("unchecked")
    public ApiResult<?> importConversation(@Body Map<String, Object> data) {
        try {
            AiConversation conversation = new AiConversation();
            conversation.setTitle((String) data.get("title"));
            BigInteger userId = BigInteger.valueOf(StpUtil.getLoginIdAsLong());
            conversation.setUserId(userId);
            AiConversation saved = conversationService.create(conversation);

            List<Map<String, Object>> messages = (List<Map<String, Object>>) data.get("messages");
            if (messages != null) {
                for (Map<String, Object> msgData : messages) {
                    AiMessage message = new AiMessage();
                    message.setConversationId(saved.getId());
                    message.setRole((String) msgData.get("role"));
                    message.setContent((String) msgData.get("content"));
                    conversationService.saveMessage(message);
                }
            }
            return ApiResult.ok(saved);
        } catch (Exception e) {
            return ApiResult.error("Import failed: " + e.getMessage());
        }
    }
}
