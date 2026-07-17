package net.cocotea.cyreneai.agent;

import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import lombok.extern.slf4j.Slf4j;
import net.cocotea.cyreneadmin.util.LoginUtils;
import net.cocotea.cyreneai.model.dto.AgentChatRequestDTO;
import net.cocotea.cyreneai.model.po.*;
import net.cocotea.cyreneai.util.ApiKeyCipher;
import org.noear.solon.annotation.Component;
import org.noear.solon.annotation.Inject;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.solon.annotation.Db;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Component
public class AgentService {

    @Db
    private LightDao lightDao;

    @Inject
    private ToolExecutionService toolExecutionService;

    private static final int MAX_ITERATIONS = 10;

    public void chatStream(AgentChatRequestDTO request, OutputStream out) throws IOException {
        BigInteger agentId = request.getAgentId();
        AiAgent agent = lightDao.load(new AiAgent(agentId));
        if (agent == null || agent.getIsDeleted() == 1 || agent.getEnableStatus() != 1) {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "智能体不存在或已禁用")));
            out.flush();
            writeSseData(out, "[DONE]");
            out.flush();
            return;
        }

        AiModel aiModel = getAiModel(agent.getModelId());
        if (aiModel == null) {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "智能体关联的模型不存在或已禁用")));
            out.flush();
            writeSseData(out, "[DONE]");
            out.flush();
            return;
        }

        ChatModel chatModel = buildChatModel(aiModel);
        if (chatModel == null) {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("error", "无法构建聊天模型")));
            out.flush();
            writeSseData(out, "[DONE]");
            out.flush();
            return;
        }

        long startTime = System.currentTimeMillis();
        int maxIter = agent.getMaxIterations() != null ? agent.getMaxIterations() : MAX_ITERATIONS;

        // Build tool specifications for the agent
        List<ToolSpecification> toolSpecs = loadToolSpecifications(agent);
        String toolInstructions = buildToolInstructions(toolSpecs);

        List<ChatMessage> messages = buildMessages(agent, request, toolInstructions, toolSpecs);
        List<Map<String, Object>> toolCallRecords = new ArrayList<>();
        AtomicInteger totalPromptTokens = new AtomicInteger(0);
        AtomicInteger totalCompletionTokens = new AtomicInteger(0);
        AtomicReference<BigDecimal> totalCostRef = new AtomicReference<>(BigDecimal.ZERO);
        BigDecimal inputPrice = aiModel.getInputPrice();
        BigDecimal outputPrice = aiModel.getOutputPrice();

        String finalResponse = "";
        int iteration = 0;

        writeSseData(out, JSONUtil.toJsonStr(Map.of("type", "agent_start", "agentName", agent.getName())));
        out.flush();

        for (iteration = 0; iteration < maxIter; iteration++) {
            writeSseData(out, JSONUtil.toJsonStr(Map.of("type", "thinking", "iteration", iteration + 1)));
            out.flush();

            ChatResponse response = chatModel.chat(messages);
            trackTokenUsage(response, totalPromptTokens, totalCompletionTokens, totalCostRef, inputPrice, outputPrice);

            String content = response.aiMessage() != null ? response.aiMessage().text() : "";
            if (content == null) content = "";

            // Try to parse tool call from response
            ToolCallResult toolCall = parseToolCall(content, toolSpecs);

            if (toolCall != null) {
                messages.add(new AiMessage(content));
                Map<String, Object> record = Map.of(
                        "iteration", iteration + 1,
                        "tool", toolCall.toolName,
                        "arguments", toolCall.arguments
                );
                toolCallRecords.add(record);

                writeSseData(out, JSONUtil.toJsonStr(Map.of(
                        "type", "tool_call",
                        "tool", toolCall.toolName,
                        "arguments", toolCall.arguments
                )));
                out.flush();

                String result;
                AiTool customTool = findCustomTool(toolCall.toolName);
                if (customTool != null) {
                    result = toolExecutionService.executeCustom(customTool, toolCall.arguments);
                } else {
                    result = toolExecutionService.executeBuiltin(toolCall.toolName, toolCall.arguments);
                }

                writeSseData(out, JSONUtil.toJsonStr(Map.of(
                        "type", "tool_result",
                        "tool", toolCall.toolName,
                        "result", result
                )));
                out.flush();

                messages.add(new UserMessage("工具 " + toolCall.toolName + " 返回结果:\n" + result));
            } else {
                finalResponse = content;
                writeSseData(out, JSONUtil.toJsonStr(Map.of("type", "content", "content", finalResponse)));
                out.flush();
                break;
            }
        }

        if (iteration >= maxIter) {
            finalResponse = "智能体已达到最大迭代次数(" + maxIter + ")，请简化问题或增加迭代次数。";
            writeSseData(out, JSONUtil.toJsonStr(Map.of("type", "content", "content", finalResponse)));
            out.flush();
        }

        long executionTime = System.currentTimeMillis() - startTime;

        writeSseData(out, JSONUtil.toJsonStr(Map.of(
                "type", "agent_complete",
                "iterationCount", iteration + 1,
                "totalPromptTokens", totalPromptTokens.get(),
                "totalCompletionTokens", totalCompletionTokens.get(),
                "totalTokens", totalPromptTokens.get() + totalCompletionTokens.get(),
                "cost", totalCostRef.get().toPlainString(),
                "executionTimeMs", executionTime
        )));
        out.flush();
        writeSseData(out, "[DONE]");
        out.flush();

        saveAgentLog(agent, request, finalResponse, iteration + 1, toolCallRecords,
                totalPromptTokens.get(), totalCompletionTokens.get(), totalCostRef.get(), executionTime);
    }

    private String buildToolInstructions(List<ToolSpecification> specs) {
        if (specs.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("\n\n## 可用工具\n");
        sb.append("你可以在需要时调用以下工具。当你需要使用工具时，请严格按照下面的JSON格式返回工具调用，不要包含其他内容：\n\n");
        sb.append("```json\n{\n  \"tool\": \"工具名称\",\n  \"arguments\": {\n    \"参数名\": \"参数值\"\n  }\n}\n```\n\n");
        sb.append("可用工具列表:\n");

        for (int i = 0; i < specs.size(); i++) {
            ToolSpecification spec = specs.get(i);
            sb.append(i + 1).append(". **").append(spec.getName()).append("**: ");
            sb.append(spec.getDescription()).append("\n");
            if (spec.getParameters() != null && spec.getParameters().containsKey("properties")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> props = (Map<String, Object>) spec.getParameters().get("properties");
                if (props != null) {
                    sb.append("   参数:\n");
                    for (var entry : props.entrySet()) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> prop = (Map<String, Object>) entry.getValue();
                        sb.append("   - `").append(entry.getKey()).append("`");
                        if (prop != null && prop.get("description") != null) {
                            sb.append(": ").append(prop.get("description"));
                        }
                        sb.append("\n");
                    }
                }
            }
        }

        sb.append("\n请根据用户的问题决定是否需要使用工具。如果不需要使用工具，直接回复用户即可。");
        return sb.toString();
    }

    private ToolCallResult parseToolCall(String content, List<ToolSpecification> specs) {
        if (content == null || content.isBlank()) return null;

        // Try to find JSON block in the response
        String jsonStr = extractJsonBlock(content);
        if (jsonStr == null) return null;

        try {
            JSONObject json = JSONUtil.parseObj(jsonStr);
            String toolName = json.getStr("tool");
            if (toolName == null || toolName.isBlank()) return null;

            // Validate tool name
            boolean validTool = specs.stream().anyMatch(s -> s.getName().equals(toolName));
            if (!validTool) {
                // Check builtin tools
                if (toolExecutionService.getSpecification(toolName) == null) return null;
            }

            JSONObject argsJson = json.getJSONObject("arguments");
            Map<String, Object> arguments = argsJson != null ? argsJson : new HashMap<>();

            return new ToolCallResult(toolName, arguments);
        } catch (Exception e) {
            log.debug("Failed to parse tool call from response: {}", content, e);
            return null;
        }
    }

    private String extractJsonBlock(String content) {
        // Try to find JSON in code block
        int startBlock = content.indexOf("```json");
        if (startBlock >= 0) {
            startBlock += 7;
            int endBlock = content.indexOf("```", startBlock);
            if (endBlock > startBlock) {
                return content.substring(startBlock, endBlock).trim();
            }
        }

        // Try to find standalone JSON object
        int braceStart = content.indexOf('{');
        if (braceStart >= 0) {
            int braceEnd = content.lastIndexOf('}');
            if (braceEnd > braceStart) {
                return content.substring(braceStart, braceEnd + 1);
            }
        }

        return null;
    }

    private void trackTokenUsage(ChatResponse response, AtomicInteger prompt, AtomicInteger completion,
                                  AtomicReference<BigDecimal> costRef, BigDecimal inputPrice, BigDecimal outputPrice) {
        if (response != null && response.metadata() != null && response.metadata().tokenUsage() != null) {
            var tu = response.metadata().tokenUsage();
            prompt.addAndGet(tu.inputTokenCount());
            completion.addAndGet(tu.outputTokenCount());
            if (inputPrice != null && outputPrice != null) {
                BigDecimal pCost = BigDecimal.valueOf(tu.inputTokenCount())
                        .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                        .multiply(inputPrice);
                BigDecimal cCost = BigDecimal.valueOf(tu.outputTokenCount())
                        .divide(BigDecimal.valueOf(1000), 6, RoundingMode.HALF_UP)
                        .multiply(outputPrice);
                costRef.updateAndGet(c -> c.add(pCost).add(cCost));
            }
        }
    }

    private List<ChatMessage> buildMessages(AiAgent agent, AgentChatRequestDTO request,
                                             String toolInstructions, List<ToolSpecification> toolSpecs) {
        List<ChatMessage> messages = new ArrayList<>();

        StringBuilder systemPrompt = new StringBuilder();
        if (agent.getSystemPrompt() != null && !agent.getSystemPrompt().isBlank()) {
            systemPrompt.append(agent.getSystemPrompt());
        } else {
            systemPrompt.append("你是一个智能助手，可以回答问题并使用各种工具来帮助用户。");
        }

        if (!toolInstructions.isBlank()) {
            systemPrompt.append(toolInstructions);
        }

        messages.add(new SystemMessage(systemPrompt.toString()));

        if (request.getHistory() != null) {
            for (var msg : request.getHistory()) {
                if (msg.getContent() == null || msg.getContent().isBlank()) continue;
                messages.add(switch (msg.getRole()) {
                    case "system" -> new SystemMessage(msg.getContent());
                    case "assistant" -> new AiMessage(msg.getContent());
                    default -> new UserMessage(msg.getContent());
                });
            }
        }

        if (request.getMessage() != null && !request.getMessage().isBlank()) {
            messages.add(new UserMessage(request.getMessage()));
        }

        return messages;
    }

    private List<ToolSpecification> loadToolSpecifications(AiAgent agent) {
        List<ToolSpecification> specs = new ArrayList<>();

        if (agent.getToolIds() != null && !agent.getToolIds().isBlank()) {
            try {
                List<String> toolIdList = JSONUtil.parseArray(agent.getToolIds()).toList(String.class);
                for (String toolIdStr : toolIdList) {
                    try {
                        BigInteger toolId = new BigInteger(toolIdStr);
                        AiTool tool = lightDao.load(new AiTool(toolId));
                        if (tool == null || tool.getIsDeleted() == 1 || tool.getEnableStatus() != 1) continue;

                        if ("builtin".equals(tool.getType()) && tool.getBuiltinHandler() != null) {
                            ToolSpecification spec = toolExecutionService.getSpecification(tool.getBuiltinHandler());
                            if (spec != null) {
                                specs.add(spec);
                            }
                        } else if ("custom".equals(tool.getType())) {
                            Map<String, Object> params = parseJsonSchema(tool.getSchemaJson());
                            specs.add(new ToolSpecification(tool.getName(), tool.getDescription(), params));
                        }
                    } catch (Exception e) {
                        log.warn("Failed to load tool spec for id={}", toolIdStr, e);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to parse toolIds for agent {}", agent.getId(), e);
            }
        }

        return specs;
    }

    private AiTool findCustomTool(String toolName) {
        try {
            Map<String, Object> params = MapUtil.newHashMap(3);
            params.put("name", toolName);
            params.put("type", "custom");
            params.put("enableStatus", 1);
            List<AiTool> tools = lightDao.find("ai_tool_findList", params, AiTool.class);
            return tools.isEmpty() ? null : tools.getFirst();
        } catch (Exception e) {
            log.warn("Failed to find custom tool: {}", toolName, e);
            return null;
        }
    }

    private Map<String, Object> parseJsonSchema(String schemaJson) {
        if (schemaJson == null || schemaJson.isBlank()) {
            return Map.of("type", "object", "properties", Map.of());
        }
        try {
            return JSONUtil.parseObj(schemaJson);
        } catch (Exception e) {
            return Map.of("type", "object", "properties", Map.of());
        }
    }

    private void saveAgentLog(AiAgent agent, AgentChatRequestDTO request, String finalResponse,
                              int iterationCount, List<Map<String, Object>> toolCallRecords,
                              int promptTokens, int completionTokens, BigDecimal cost, long executionTimeMs) {
        try {
            AiAgentLog logEntry = new AiAgentLog()
                    .setAgentId(agent.getId())
                    .setAgentName(agent.getName())
                    .setConversationId(request.getConversationId())
                    .setUserId(LoginUtils.loginId())
                    .setUserInput(request.getMessage())
                    .setFinalResponse(finalResponse)
                    .setIterationCount(iterationCount)
                    .setToolCalls(JSONUtil.toJsonStr(toolCallRecords))
                    .setPromptTokens(promptTokens)
                    .setCompletionTokens(completionTokens)
                    .setTotalTokens(promptTokens + completionTokens)
                    .setCost(cost)
                    .setStatus("success")
                    .setExecutionTimeMs(executionTimeMs)
                    .setCreatedTime(LocalDateTime.now());
            lightDao.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to save agent log", e);
        }
    }

    private AiModel getAiModel(BigInteger modelId) {
        if (modelId == null) return null;
        AiModel model = lightDao.load(new AiModel(modelId));
        if (model == null || model.getIsDeleted() == 1 || model.getEnableStatus() != 1) return null;
        return model;
    }

    private ChatModel buildChatModel(AiModel model) {
        AiModelProvider provider = lightDao.load(new AiModelProvider(model.getProviderId()));
        if (provider == null || provider.getIsDeleted() == 1 || provider.getEnableStatus() != 1) return null;

        String type = provider.getProviderType();
        String apiKey = ApiKeyCipher.decrypt(provider.getApiKey() != null ? provider.getApiKey() : "");
        String baseUrl = provider.getApiBaseUrl();
        String modelName = model.getModelName();

        return switch (type.toLowerCase()) {
            case "dashscope" -> QwenChatModel.builder()
                    .apiKey(apiKey).modelName(modelName).build();
            case "openai" -> OpenAiChatModel.builder()
                    .apiKey(apiKey).modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.openai.com")
                    .build();
            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(apiKey).modelName(modelName)
                    .baseUrl(baseUrl != null ? baseUrl : "https://api.anthropic.com")
                    .build();
            case "ollama" -> OllamaChatModel.builder()
                    .baseUrl(baseUrl != null ? baseUrl : "http://localhost:11434")
                    .modelName(modelName).build();
            case "gemini" -> GoogleAiGeminiChatModel.builder()
                    .apiKey(apiKey).modelName(modelName).build();
            case "custom" -> OpenAiChatModel.builder()
                    .apiKey(apiKey).modelName(modelName)
                    .baseUrl(baseUrl).build();
            default -> {
                log.warn("unsupported provider type: {}", type);
                yield null;
            }
        };
    }

    private void writeSseData(OutputStream out, String data) throws IOException {
        String event = "data: " + data + "\n\n";
        out.write(event.getBytes(StandardCharsets.UTF_8));
    }

    private record ToolCallResult(String toolName, Map<String, Object> arguments) {}
}
