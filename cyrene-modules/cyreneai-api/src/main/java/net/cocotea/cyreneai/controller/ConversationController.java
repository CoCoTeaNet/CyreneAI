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

    @Post @Mapping("/delete")
    public ApiResult<?> delete(@Body BigInteger id) {
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

    @Post @Mapping("/deleteMessage")
    public ApiResult<?> deleteMessage(@Body BigInteger id) {
        conversationService.deleteMessage(id);
        return ApiResult.ok();
    }

    @Post @Mapping("/clearMessages")
    public ApiResult<?> clearMessages(@Body BigInteger conversationId) {
        conversationService.clearMessages(conversationId);
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
