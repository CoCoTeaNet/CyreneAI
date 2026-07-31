package net.cocotea.cyreneai.controller;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO.ChatMessageDTO;
import net.cocotea.cyreneai.model.dto.ChatRequestDTO.ContentPart;
import net.cocotea.cyreneai.model.po.AiConversation;
import net.cocotea.cyreneai.model.po.AiModel;
import net.cocotea.cyreneai.model.po.AiModelProvider;
import net.cocotea.cyreneai.model.po.AiApiKey;
import net.cocotea.cyreneai.model.po.AiAuditLog;
import net.cocotea.cyreneai.model.vo.AiRetrievalResultVO;
import net.cocotea.cyreneai.service.AiApiKeyService;
import net.cocotea.cyreneai.service.AiAuditLogService;
import net.cocotea.cyreneai.service.AiConversationService;
import net.cocotea.cyreneai.service.AiQuotaAlertService;
import net.cocotea.cyreneai.service.ChatModelFactory;
import net.cocotea.cyreneai.service.governance.ContentSafetyResult;
import net.cocotea.cyreneai.service.governance.ContentSafetyService;
import net.cocotea.cyreneai.service.governance.RateLimitService;
import net.cocotea.cyreneai.service.rag.KnowledgeBaseService;
import net.cocotea.cyreneai.util.TokenEstimator;
import org.noear.solon.Solon;
import org.noear.solon.annotation.Body;
import org.noear.solon.annotation.Controller;
import org.noear.solon.annotation.Get;
import org.noear.solon.annotation.Inject;
import org.noear.solon.annotation.Mapping;
import org.noear.solon.annotation.Post;
import org.noear.solon.core.handle.Context;
import org.noear.solon.validation.annotation.Valid;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@Valid
@Controller
@Mapping("/ai/chat")
public class ChatController {

    @Db
    private LightDao lightDao;

    @Inject
    private AiConversationService conversationService;

    @Inject
    private KnowledgeBaseService knowledgeBaseService;

    @Inject
    private ContentSafetyService contentSafetyService;

    @Inject
    private AiAuditLogService aiAuditLogService;

    @Inject
    private AiApiKeyService aiApiKeyService;

    @Inject
    private AiQuotaAlertService aiQuotaAlertService;

    @Inject
    private RateLimitService rateLimitService;

    @Inject
    private ChatModelFactory chatModelFactory;

    /** 流式响应等待超时（分钟），防止模型不回调导致请求线程永久阻塞 */
    private static final long STREAM_AWAIT_TIMEOUT_MINUTES = 10;

    /** 流式模型实例缓存上限（LRU），避免重复构建 ChatModel。 */
    private static final int MODEL_CACHE_MAX = 64;

    /** SSE 合并发送阈值（字符数），减少 flush 次数、缓解网络 I/O 压力（背压/流控）。 */
    private static final int SSE_FLUSH_THRESHOLD = 64;

    /**
     * 流式模型实例缓存：langchain4j 的 StreamingChatModel 不可变且线程安全，
     * 按 提供商+模型+参数 复用，减少重复构建带来的开销。
     */
    private static final Map<String, StreamingChatModel> STREAMING_MODEL_CACHE =
            Collections.synchronizedMap(new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, StreamingChatModel> eldest) {
                    return size() > MODEL_CACHE_MAX;
                }
            });

    @Get @Mapping("/ping")
    public String ping() {
        ChatModel model = buildDefaultModel();
        if (model == null) {
            return "Error: no enabled model provider found in database";
        }
        return model.chat("Ping! ~~~///(^v^)\\\\~~~");
    }

    @Post @Mapping("/stream")
    public void stream(@Body ChatRequestDTO request) throws IOException {
        Context ctx = Context.current();
        ctx.contentType("text/event-stream;charset=utf-8");
        ctx.headerAdd("Cache-Control", "no-cache");
        ctx.headerAdd("Connection", "keep-alive");
        ctx.headerAdd("X-Accel-Buffering", "no");

        OutputStream out = ctx.outputStream();
        CountDownLatch latch = new CountDownLatch(1);
        StringBuilder responseContent = new StringBuilder();
        final int[] tokenCounts = new int[3]; // prompt, completion, total
        final BigDecimal[] cost = new BigDecimal[1]; // calculated cost
        final long startNanos = System.nanoTime();
        final String[] auditStatus = new String[]{"success"};
        final String[] auditError = new String[]{null};
        final AiModel[] auditModel = new AiModel[1];

        // 输入内容安全检查
        String lastUserText = extractLastUserText(request);
        if (lastUserText != null && !lastUserText.isEmpty()) {
            try {
                ContentSafetyResult safety = contentSafetyService.check(lastUserText, "input");
                if (safety != null && !safety.isAllowed()) {
                    writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "内容安全拦截: " + safety.getReason())));
                    out.flush();
                    writeSseData(out, "[DONE]");
                    out.flush();
                    recordAudit(request, null, tokenCounts, cost[0], startNanos, "blocked", safety.getReason());
                    return;
                }
            } catch (Exception se) {
                log.error("content safety check failed", se);
            }
        }

        // 限流/配额：携带平台 API Key（sk-cyr-）时执行 Key 级 RPM/TPM/月度 Token 配额校验
        final AiApiKey apiKey = resolveApiKey(ctx);
        final int estimatedInputTokens = estimateRequestTokens(request);
        RateLimitService.Result limit = rateLimitService.check(apiKey, estimatedInputTokens);
        if (!limit.allowed()) {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("error", limit.message())));
            out.flush();
            writeSseData(out, "[DONE]");
            out.flush();
            recordAudit(request, null, tokenCounts, cost[0], startNanos, limit.reason(), limit.message());
            if (apiKey != null) {
                aiApiKeyService.recordUsage(apiKey.getId(), apiKey.getUserId(), 0, 0, null, limit.reason());
            }
            return;
        }

        try {
            AiModel aiModel = getAiModel(request.getModelId());
            if (aiModel == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "No available model")));
                out.flush();
                return;
            }
            auditModel[0] = aiModel;

            // If editing a message, truncate all messages after (and including) the edited one
            if (request.getConversationId() != null && request.getEditMessageId() != null) {
                conversationService.truncateMessages(request.getConversationId(), request.getEditMessageId());
                log.info("Truncated messages after editMessageId={}", request.getEditMessageId());
            }

            // 先持久化用户消息，避免客户端中断或服务异常时丢失
            if (request.getConversationId() != null) {
                saveUserMessage(request);
            }

            BigDecimal inputPrice = aiModel.getInputPrice();
            BigDecimal outputPrice = aiModel.getOutputPrice();

            StreamingChatModel model = buildStreamingModel(aiModel, request);
            if (model == null) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "No available model provider")));
                out.flush();
                return;
            }

            // 先执行知识库检索，将命中的文档片段作为引用来源通过 SSE 提前推送给前端展示
            List<AiRetrievalResultVO> relevantDocs = retrieveRagDocs(request.getKbId(), request.getMessages());
            if (!relevantDocs.isEmpty()) {
                writeSseData(out, JSONUtil.toJsonStr(Map.of(
                        "type", "sources",
                        "sources", buildSourcePayload(relevantDocs)
                )));
                out.flush();
            }

            List<ChatMessage> messages = convertMessages(request.getMessages(), request.getSystemPrompt(), relevantDocs);
            messages = compressMessages(messages, aiModel, request);
            // 背压/流控：合并待发片段、检测客户端断开
            final StringBuilder sendBuffer = new StringBuilder();
            final boolean[] clientGone = new boolean[]{false};
            model.chat(messages, new StreamingChatResponseHandler() {
                @Override
                public void onPartialResponse(String token) {
                    if (clientGone[0]) {
                        return;
                    }
                    responseContent.append(token);
                    sendBuffer.append(token);
                    // 累计超过阈值才刷新，降低 flush 频次
                    if (sendBuffer.length() >= SSE_FLUSH_THRESHOLD) {
                        flushSseBuffer(out, sendBuffer, clientGone);
                    }
                }

                @Override
                public void onCompleteResponse(ChatResponse response) {
                    try {
                        // 先冲刷剩余缓冲内容
                        flushSseBuffer(out, sendBuffer, clientGone);
                        // 输出内容安全审核：block 替换全文，replace 推送净化后内容
                        applyOutputSafety(out, responseContent, auditStatus, auditError);
                        // 部分提供商在流式结束时可能回传 null response（例如未返回用量统计的兼容端点），
                        // 此时按字符估算兑底，保证成本与审计数据不缺失（Ollama/custom 等）
                        if (response != null && response.metadata() != null && response.metadata().tokenUsage() != null) {
                            var tokenUsage = response.metadata().tokenUsage();
                            tokenCounts[0] = tokenUsage.inputTokenCount();
                            tokenCounts[1] = tokenUsage.outputTokenCount();
                        } else {
                            tokenCounts[0] = estimatedInputTokens;
                            tokenCounts[1] = estimateTokens(responseContent.toString());
                        }
                        tokenCounts[2] = tokenCounts[0] + tokenCounts[1];

                        // Calculate cost
                        if (inputPrice != null && outputPrice != null) {
                            BigDecimal promptCost = BigDecimal.valueOf(tokenCounts[0])
                                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                                    .multiply(inputPrice);
                            BigDecimal completionCost = BigDecimal.valueOf(tokenCounts[1])
                                    .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                                    .multiply(outputPrice);
                            cost[0] = promptCost.add(completionCost).setScale(6, RoundingMode.HALF_UP);
                        }

                        writeSseData(out, JSONUtil.toJsonStr(Map.of(
                                "type", "token_usage",
                                "promptTokens", tokenCounts[0],
                                "completionTokens", tokenCounts[1],
                                "totalTokens", tokenCounts[2],
                                "cost", cost[0] != null ? cost[0].toPlainString() : "0"
                        )));
                        writeSseData(out, "[DONE]");
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE flush on complete error", e);
                    } finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onError(Throwable error) {
                    try {
                        auditStatus[0] = "error";
                        auditError[0] = error.getMessage();
                        writeSseData(out, JSONUtil.toJsonStr(Map.of("error", error.getMessage())));
                        out.flush();
                    } catch (IOException e) {
                        log.error("SSE write error on error", e);
                    } finally {
                        latch.countDown();
                    }
                }
            });
        } catch (Exception e) {
            log.error("Streaming chat error", e);
            auditStatus[0] = "error";
            auditError[0] = e.getMessage();
            try {
                writeSseData(out, JSONUtil.toJsonStr(Map.of("error", e.getMessage())));
                out.flush();
            } catch (IOException ex) {
                log.error("SSE write error", ex);
            } finally {
                latch.countDown();
            }
        }

        try {
            // 防止底层模型不回调 onComplete/onError 导致请求线程永久阻塞
            if (!latch.await(STREAM_AWAIT_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
                log.warn("streaming chat await timeout after {} minutes", STREAM_AWAIT_TIMEOUT_MINUTES);
                auditStatus[0] = "error";
                auditError[0] = "streaming timeout";
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 记录审计日志
        recordAudit(request, auditModel[0], tokenCounts, cost[0], startNanos, auditStatus[0], auditError[0]);

        // Key 级限流计数、用量落库（含月度 Token 累加）与配额告警评估
        if (apiKey != null) {
            rateLimitService.increment(apiKey, tokenCounts[2]);
            aiApiKeyService.recordUsage(apiKey.getId(), apiKey.getUserId(),
                    tokenCounts[0], tokenCounts[1], cost[0], auditStatus[0]);
            try {
                AiApiKey fresh = lightDao.load(new AiApiKey(apiKey.getId()));
                aiQuotaAlertService.evaluate(fresh != null ? fresh : apiKey);
            } catch (Exception e) {
                log.error("evaluate quota alert failed", e);
            }
        }

        // Save assistant message to database if conversationId is provided
        if (request.getConversationId() != null) {
            saveAssistantMessage(request, responseContent.toString(), tokenCounts, cost[0]);
        }
    }

    /**
     * 流式开始前先持久化用户消息，避免客户端中断或服务崩溃时丢失
     */
    private void saveUserMessage(ChatRequestDTO request) {
        try {
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                ChatMessageDTO lastUserMsg = request.getMessages().getLast();
                if ("user".equals(lastUserMsg.getRole())) {
                    net.cocotea.cyreneai.model.po.AiMessage userMessage = new net.cocotea.cyreneai.model.po.AiMessage();
                    userMessage.setConversationId(request.getConversationId());
                    userMessage.setRole("user");
                    userMessage.setContent(lastUserMsg.getContent());
                    conversationService.saveMessage(userMessage);

                    // Auto-generate title from the first user message
                    autoGenerateTitle(request.getConversationId(), lastUserMsg.getContent());
                }
            }
        } catch (Exception e) {
            log.error("Failed to save user message", e);
        }
    }

    private void saveAssistantMessage(ChatRequestDTO request, String responseContent, int[] tokenCounts, BigDecimal cost) {
        try {
            if (!responseContent.isEmpty()) {
                net.cocotea.cyreneai.model.po.AiMessage assistantMessage = new net.cocotea.cyreneai.model.po.AiMessage();
                assistantMessage.setConversationId(request.getConversationId());
                assistantMessage.setRole("assistant");
                assistantMessage.setContent(responseContent);
                assistantMessage.setPromptTokens(tokenCounts[0]);
                assistantMessage.setCompletionTokens(tokenCounts[1]);
                assistantMessage.setTotalTokens(tokenCounts[2]);
                assistantMessage.setCost(cost);
                conversationService.saveMessage(assistantMessage);
            }
        } catch (Exception e) {
            log.error("Failed to save assistant message", e);
        }
    }

    private void autoGenerateTitle(BigInteger conversationId, String userMessageContent) {
        try {
            AiConversation conv = conversationService.findById(conversationId);
            if (conv == null) return;
            // Only generate title if current title is default
            if (conv.getTitle() != null && !"New Chat".equals(conv.getTitle())) return;
            String title = userMessageContent.strip();
            if (title.length() > 30) {
                title = title.substring(0, 30) + "...";
            }
            conversationService.updateTitle(conversationId, title);
        } catch (Exception e) {
            log.error("Failed to auto-generate title", e);
        }
    }

    private void writeSseData(OutputStream out, String data) throws IOException {
        String event = "data: " + data + "\n\n";
        out.write(event.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 背压/流控：将缓冲区内容作为单个 SSE data 帧发送并 flush，减少 flush 频次；
     * 写入失败视为客户端已断开，置位 clientGone 以便尽早停止推送。
     */
    private void flushSseBuffer(OutputStream out, StringBuilder buffer, boolean[] clientGone) {
        if (clientGone[0] || buffer.isEmpty()) {
            return;
        }
        try {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("content", buffer.toString())));
            out.flush();
            buffer.setLength(0);
        } catch (IOException e) {
            clientGone[0] = true;
            log.warn("SSE client disconnected, stop streaming");
        }
    }

    private AiModel getAiModel(BigInteger modelId) {
        if (modelId != null) {
            AiModel model = lightDao.load(new AiModel(modelId));
            if (model == null || model.getIsDeleted() == 1 || model.getEnableStatus() != 1) {
                return null;
            }
            return model;
        } else {
            Map<String, Object> modelParam = MapUtil.newHashMap(2);
            modelParam.put("isDefault", 1);
            modelParam.put("enableStatus", 1);
            List<AiModel> models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
            if (models.isEmpty()) {
                modelParam.remove("isDefault");
                models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
            }
            if (models.isEmpty()) {
                return null;
            }
            return models.getFirst();
        }
    }

    private StreamingChatModel buildStreamingModel(AiModel model, ChatRequestDTO request) {
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        if (provider == null || provider.getIsDeleted() == 1 || provider.getEnableStatus() != 1) {
            return null;
        }

        log.info("model: {}", model);

        // 命中缓存则复用，避免重复构建
        String cacheKey = String.join("|", provider.getProviderType(),
                String.valueOf(provider.getId()), String.valueOf(provider.getUpdateTime()),
                String.valueOf(model.getModelName()), String.valueOf(request.getTemperature()),
                String.valueOf(request.getTopP()), String.valueOf(request.getMaxTokens()));
        StreamingChatModel cached = STREAMING_MODEL_CACHE.get(cacheKey);
        if (cached != null) {
            return cached;
        }

        StreamingChatModel built = chatModelFactory.streaming(provider, model,
                request.getTemperature(), request.getTopP(), request.getMaxTokens());
        if (built != null) {
            STREAMING_MODEL_CACHE.put(cacheKey, built);
        }
        return built;
    }

    /**
     * 知识库检索：抽取最后一条用户消息作为查询，返回命中的文档片段。
     * 检索策略传 {@code null}，由知识库自身配置（top_k / mmr / rerank）决定。
     */
    private List<AiRetrievalResultVO> retrieveRagDocs(BigInteger kbId, List<ChatMessageDTO> dtos) {
        if (kbId == null || dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            String lastUserQuery = null;
            for (int i = dtos.size() - 1; i >= 0; i--) {
                if ("user".equals(dtos.get(i).getRole())) {
                    lastUserQuery = dtos.get(i).getContent();
                    break;
                }
            }
            if (lastUserQuery == null) {
                return Collections.emptyList();
            }
            return knowledgeBaseService.retrieve(kbId, lastUserQuery, 5, 0.7, null);
        } catch (Exception e) {
            log.warn("Failed to retrieve RAG context", e);
            return Collections.emptyList();
        }
    }

    /**
     * 构建 SSE 引用来源事件负载，供前端展示匹配到的文档片段。
     */
    private List<Map<String, Object>> buildSourcePayload(List<AiRetrievalResultVO> relevantDocs) {
        List<Map<String, Object>> sources = new ArrayList<>();
        for (AiRetrievalResultVO doc : relevantDocs) {
            Map<String, Object> src = new LinkedHashMap<>();
            src.put("documentId", doc.getDocumentId() != null ? doc.getDocumentId().toString() : null);
            src.put("documentName", doc.getDocumentName());
            src.put("content", doc.getContent());
            src.put("score", doc.getScore());
            sources.add(src);
        }
        return sources;
    }

    private List<ChatMessage> convertMessages(List<ChatMessageDTO> dtos, String systemPrompt, List<AiRetrievalResultVO> relevantDocs) {
        List<ChatMessage> messages = new ArrayList<>();

        // Inject knowledge base context as system prompt
        if (relevantDocs != null && !relevantDocs.isEmpty()) {
            StringBuilder ragContext = new StringBuilder();
            ragContext.append("以下是来自知识库的相关参考信息，请基于这些信息回答用户问题。\n\n");
            for (int i = 0; i < relevantDocs.size(); i++) {
                AiRetrievalResultVO doc = relevantDocs.get(i);
                ragContext.append("[").append(i + 1).append("] ");
                if (doc.getDocumentName() != null) {
                    ragContext.append("(来源: ").append(doc.getDocumentName()).append(") ");
                }
                ragContext.append(doc.getContent()).append("\n\n");
            }
            ragContext.append("请基于上述参考信息回答用户的问题。如果参考信息不足以回答问题，请说明。");
            messages.add(new SystemMessage(ragContext.toString()));
            log.info("Injected {} RAG context chunks", relevantDocs.size());
        }

        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(new SystemMessage(systemPrompt));
        }
        if (dtos == null) return messages;
        for (ChatMessageDTO dto : dtos) {
            if (dto.getContent() == null || dto.getContent().isBlank()) {
                if (dto.getContentParts() == null || dto.getContentParts().isEmpty()) continue;
            }
            if (dto.getContentParts() != null && !dto.getContentParts().isEmpty()) {
                List<Content> contents = new ArrayList<>();
                boolean hasImage = false;
                for (ContentPart part : dto.getContentParts()) {
                    if ("text".equals(part.getType())) {
                        contents.add(new TextContent(part.getText()));
                    } else if ("image_url".equals(part.getType())) {
                        String url = part.getImageUrl();
                        if (url != null && !url.isBlank()) {
                            contents.add(ImageContent.from(url));
                            hasImage = true;
                        }
                    }
                }
                if (!contents.isEmpty()) {
                    messages.add(new UserMessage(contents));
                    continue;
                }
                if (!hasImage) continue;
            }
            messages.add(switch (dto.getRole()) {
                case "system" -> new SystemMessage(dto.getContent());
                case "assistant" -> new AiMessage(dto.getContent());
                default -> new UserMessage(dto.getContent());
            });
        }
        return messages;
    }

    private List<ChatMessage> compressMessages(List<ChatMessage> messages, AiModel aiModel, ChatRequestDTO request) {
        Integer contextWindow = aiModel.getContextWindow();
        if (contextWindow == null || contextWindow <= 0) {
            return messages;
        }

        // Token 估算：区分 CJK 与其他字符，避免中文严重低估
        int estimatedTokens = 0;
        for (ChatMessage msg : messages) {
            estimatedTokens += estimateTokens(getTextFromMessage(msg));
        }

        // If within limit, return as is
        if (estimatedTokens <= contextWindow * 0.8) {
            return messages;
        }

        // Use summarization strategy if requested
        if ("summarize".equals(request.getContextStrategy())) {
            return summarizeMessages(messages, aiModel, contextWindow);
        }

        // Default: truncate oldest non-system messages
        List<ChatMessage> truncated = new ArrayList<>();
        int remainingTokens = (int) (contextWindow * 0.8);

        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) {
                remainingTokens -= estimateTokens(getTextFromMessage(msg));
                truncated.add(msg);
            }
        }

        List<ChatMessage> nonSystemMessages = messages.stream()
                .filter(m -> !(m instanceof SystemMessage))
                .toList();

        // 插入点固定在 system 消息之后（可能为 0，避免空列表 add(1) 越界）；newest 先插、older 顶右，自然恢复时序
        final int insertPos = truncated.size();
        for (int i = nonSystemMessages.size() - 1; i >= 0; i--) {
            ChatMessage msg = nonSystemMessages.get(i);
            int msgTokens = estimateTokens(getTextFromMessage(msg));
            if (remainingTokens >= msgTokens) {
                remainingTokens -= msgTokens;
                truncated.add(insertPos, msg);
            } else {
                break;
            }
        }

        log.info("Truncated messages from {} to {} (estimated tokens: {} -> {})",
                messages.size(), truncated.size(), estimatedTokens, estimatedTokens - remainingTokens);
        return truncated;
    }

    private List<ChatMessage> summarizeMessages(List<ChatMessage> messages, AiModel aiModel, Integer contextWindow) {
        int targetTokens = (int) (contextWindow * 0.5);
        List<ChatMessage> keep = new ArrayList<>();
        List<ChatMessage> toSummarize = new ArrayList<>();
        int accumulated = 0;

        // Collect messages to summarize (oldest first) until we hit half the budget
        for (ChatMessage msg : messages) {
            String text = getTextFromMessage(msg);
            int msgTokens = estimateTokens(text);
            if (msg instanceof SystemMessage) {
                keep.add(msg);
            } else if (accumulated + msgTokens <= targetTokens) {
                accumulated += msgTokens;
                toSummarize.add(msg);
            } else if (accumulated > 0) {
                break;
            } else {
                // Single message exceeds budget, keep it anyway
                keep.add(msg);
            }
        }

        if (toSummarize.isEmpty()) {
            return messages;
        }

        // Build summarization prompt
        StringBuilder summaryInput = new StringBuilder();
        summaryInput.append("Please summarize the following conversation concisely to preserve context:\n\n");
        for (ChatMessage msg : toSummarize) {
            String role = msg instanceof UserMessage ? "User" : "Assistant";
            summaryInput.append(role).append(": ").append(getTextFromMessage(msg)).append("\n");
        }
        summaryInput.append("\nSummary:");

        try {
            AiModelProvider provider = lightDao.load(new AiModelProvider(aiModel.getProviderId()));
            if (provider != null && provider.getIsDeleted() == 0 && provider.getEnableStatus() == 1) {
                ChatModel chatModel = chatModelFactory.blocking(provider, aiModel);
                if (chatModel != null) {
                    String summary = chatModel.chat(summaryInput.toString());
                    keep.add(0, new SystemMessage("Previous conversation summary: " + summary));
                    log.info("Summarized {} messages into a summary", toSummarize.size());
                }
            }
        } catch (Exception e) {
            log.warn("Summarization failed, falling back to truncation", e);
            // Fallback: 重建列表只保留最近几条消息，避免与前面累积的 keep 重复添加 system 消息
            List<ChatMessage> fallback = new ArrayList<>();
            int fallbackTokens = (int) (contextWindow * 0.8);
            for (ChatMessage msg : messages) {
                if (msg instanceof SystemMessage) {
                    fallbackTokens -= estimateTokens(getTextFromMessage(msg));
                    fallback.add(msg);
                }
            }
            // 插入点固定在 system 消息之后（可能为 0，避免空列表 add(1) 越界）
            final int fallbackInsertPos = fallback.size();
            for (int i = messages.size() - 1; i >= 0; i--) {
                ChatMessage msg = messages.get(i);
                if (msg instanceof SystemMessage) continue;
                int msgTokens = estimateTokens(getTextFromMessage(msg));
                if (fallbackTokens >= msgTokens) {
                    fallbackTokens -= msgTokens;
                    fallback.add(fallbackInsertPos, msg);
                } else {
                    break;
                }
            }
            return fallback;
        }

        // Add remaining messages that weren't summarized
        boolean summarizingDone = false;
        for (ChatMessage msg : messages) {
            if (msg instanceof SystemMessage) continue;
            if (!summarizingDone && !toSummarize.contains(msg)) {
                summarizingDone = true;
            }
            if (summarizingDone && !toSummarize.contains(msg)) {
                keep.add(msg);
            }
        }

        log.info("Summarized context: kept {} messages after summarization", keep.size());
        return keep;
    }

    private String getTextFromMessage(ChatMessage msg) {
        if (msg instanceof SystemMessage sm) {
            return sm.text();
        } else if (msg instanceof AiMessage am) {
            return am.text();
        } else if (msg instanceof UserMessage um) {
            return um.singleText();
        }
        return "";
    }

    private ChatModel buildDefaultModel() {
        Map<String, Object> modelParam = MapUtil.newHashMap(2);
        modelParam.put("isDefault", 1);
        modelParam.put("enableStatus", 1);
        List<AiModel> models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
        if (models.isEmpty()) {
            modelParam.remove("isDefault");
            models = lightDao.find("ai_model_findList", modelParam, AiModel.class);
        }
        if (models.isEmpty()) {
            return null;
        }
        AiModel model = models.getFirst();

        Map<String, Object> providerParam = MapUtil.newHashMap(1);
        providerParam.put("id", model.getProviderId());
        List<AiModelProvider> providers = lightDao.find("ai_model_provider_findList", providerParam, AiModelProvider.class);
        if (providers.isEmpty()) {
            return null;
        }
        AiModelProvider provider = providers.getFirst();
        return chatModelFactory.blocking(provider, model);
    }

    /**
     * 从请求中抽取最后一条用户消息文本(仅文本部分)用于内容安全检查
     */
    private String extractLastUserText(ChatRequestDTO request) {
        if (request == null || request.getMessages() == null || request.getMessages().isEmpty()) return null;
        for (int i = request.getMessages().size() - 1; i >= 0; i--) {
            ChatMessageDTO msg = request.getMessages().get(i);
            if ("user".equals(msg.getRole()) && msg.getContent() != null) {
                return msg.getContent();
            }
        }
        return null;
    }

    /**
     * 记录审计日志(不抛出异常, 不影响主链路)
     */
    private void recordAudit(ChatRequestDTO request, AiModel model, int[] tokenCounts,
                             BigDecimal cost, long startNanos, String status, String errorMsg) {
        try {
            AiAuditLog log = new AiAuditLog();
            log.setEndpoint("/ai/chat/stream");
            log.setHttpMethod("POST");
            log.setStatus(status == null ? "unknown" : status);
            log.setErrorMsg(errorMsg);
            log.setPromptTokens(tokenCounts != null ? tokenCounts[0] : 0);
            log.setCompletionTokens(tokenCounts != null ? tokenCounts[1] : 0);
            log.setTotalTokens(tokenCounts != null ? tokenCounts[2] : 0);
            log.setCost(cost);
            log.setLatencyMs((System.nanoTime() - startNanos) / 1_000_000L);
            if (request != null) {
                log.setConversationId(request.getConversationId());
            }
            if (model != null) {
                log.setModelId(model.getId());
                log.setModelName(model.getModelName());
            }
            // 审计留存的用户输入片段长度可配置；配置为 0 或负数时不留存原文（隐私脱敏）
            int snippetLen = Solon.cfg().getInt("myapp.ai.audit-prompt-snippet-len", 500);
            if (snippetLen > 0) {
                String prompt = extractLastUserText(request);
                if (prompt != null) {
                    log.setPromptSnippet(prompt.length() > snippetLen ? prompt.substring(0, snippetLen) : prompt);
                }
            }
            aiAuditLogService.record(log);
        } catch (Exception e) {
            ChatController.log.error("record chat audit failed", e);
        }
    }

    /** 平台 API Key 明文前缀 */
    private static final String API_KEY_PREFIX = "sk-cyr-";

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

    /**
     * Token 估算：CJK 字符约 1.5 字符/token，其余约 4 字符/token（统一按 4 字符会对中文严重低估）
     */
    private int estimateTokens(String text) {
        return TokenEstimator.estimate(text);
    }

    /**
     * 估算整个请求（全部消息 + 系统提示词）的输入 Token，用于限流预判与用量兑底
     */
    private int estimateRequestTokens(ChatRequestDTO request) {
        int total = 0;
        if (request == null) {
            return total;
        }
        if (request.getMessages() != null) {
            for (ChatMessageDTO msg : request.getMessages()) {
                total += estimateTokens(msg.getContent());
            }
        }
        total += estimateTokens(request.getSystemPrompt());
        return total;
    }

    /**
     * 输出内容安全审核：已流式发出的内容无法撤回，
     * 拦截/净化时通过 content_replaced 事件通知前端替换展示，并以净化后文本持久化与审计
     */
    private void applyOutputSafety(OutputStream out, StringBuilder responseContent,
                                   String[] auditStatus, String[] auditError) {
        if (responseContent.isEmpty()) {
            return;
        }
        try {
            ContentSafetyResult safety = contentSafetyService.check(responseContent.toString(), "output");
            if (safety == null) {
                return;
            }
            if (!safety.isAllowed()) {
                auditStatus[0] = "blocked";
                auditError[0] = "输出内容安全拦截: " + safety.getReason();
                responseContent.setLength(0);
                responseContent.append("内容因安全策略被拦截。");
                writeSseData(out, JSONUtil.toJsonStr(Map.of(
                        "type", "content_replaced",
                        "content", responseContent.toString(),
                        "error", auditError[0]
                )));
                out.flush();
            } else if ("replace".equals(safety.getAction()) && safety.getSanitizedText() != null
                    && !safety.getSanitizedText().equals(responseContent.toString())) {
                responseContent.setLength(0);
                responseContent.append(safety.getSanitizedText());
                writeSseData(out, JSONUtil.toJsonStr(Map.of(
                        "type", "content_replaced",
                        "content", responseContent.toString()
                )));
                out.flush();
            }
        } catch (Exception e) {
            log.error("output content safety check failed", e);
        }
    }
}
