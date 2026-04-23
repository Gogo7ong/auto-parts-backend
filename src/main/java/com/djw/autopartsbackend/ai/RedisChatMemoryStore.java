package com.djw.autopartsbackend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Redis会话记忆存储
 * 使用Redis持久化存储AI对话历史，支持多轮对话
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String TOOL_CALL_PLACEHOLDER = "[tool-call]";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 会话过期时间（7天）
     */
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    /**
     * Redis Key前缀
     */
    private static final String KEY_PREFIX = "ai:chat:memory:";

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = getKey(memoryId);
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);
        
        if (jsonList == null || jsonList.isEmpty()) {
            log.debug("获取会话记忆: memoryId={}, 消息数=0", memoryId);
            return new ArrayList<>();
        }
        
        List<ChatMessage> messages = new ArrayList<>();
        for (String json : jsonList) {
            try {
                ChatMessage message = deserializeMessage(json);
                if (message != null) {
                    messages.add(message);
                }
            } catch (JsonProcessingException e) {
                log.warn("反序列化消息失败: {}", e.getMessage());
            }
        }
        
        log.debug("获取会话记忆: memoryId={}, 消息数={}", memoryId, messages.size());
        return messages;
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = getKey(memoryId);
        
        // 先删除旧消息
        redisTemplate.delete(key);
        
        if (messages != null && !messages.isEmpty()) {
            List<String> jsonList = new ArrayList<>();
            for (ChatMessage message : messages) {
                try {
                    String json = serializeMessage(message);
                    if (json != null) {
                        jsonList.add(json);
                    }
                } catch (JsonProcessingException e) {
                    log.warn("序列化消息失败: {}", e.getMessage());
                }
            }
            
            if (!jsonList.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(key, jsonList);
                redisTemplate.expire(key, SESSION_TTL);
            }
            
            log.debug("更新会话记忆: memoryId={}, 消息数={}", memoryId, messages.size());
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = getKey(memoryId);
        redisTemplate.delete(key);
        log.debug("删除会话记忆: memoryId={}", memoryId);
    }

    /**
     * 构建Redis Key
     */
    private String getKey(Object memoryId) {
        return KEY_PREFIX + memoryId.toString();
    }

    /**
     * 序列化消息为JSON
     */
    private String serializeMessage(ChatMessage message) throws JsonProcessingException {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setType(message.type().name());
        
        switch (message.type()) {
            case SYSTEM -> wrapper.setContent(((SystemMessage) message).text());
            case USER -> wrapper.setContent(((UserMessage) message).singleText());
            case AI -> {
                AiMessage aiMessage = (AiMessage) message;
                if (aiMessage.toolExecutionRequests() != null && !aiMessage.toolExecutionRequests().isEmpty()) {
                    wrapper.setContent(normalizeAiContent(aiMessage.text()));
                    wrapper.setHasToolCalls(true);
                    List<ToolCallInfo> toolCalls = aiMessage.toolExecutionRequests().stream()
                        .map(req -> new ToolCallInfo(req.id(), req.name(), req.arguments()))
                        .toList();
                    wrapper.setToolCalls(toolCalls);
                } else {
                    wrapper.setContent(aiMessage.text());
                }
            }
            case TOOL_EXECUTION_RESULT -> {
                ToolExecutionResultMessage toolResult = (ToolExecutionResultMessage) message;
                wrapper.setContent(toolResult.text());
                wrapper.setToolCallId(toolResult.id());
                wrapper.setToolName(toolResult.toolName());
            }
            default -> {
                log.debug("未处理的消息类型: {}", message.type());
                return null;
            }
        }
        
        return objectMapper.writeValueAsString(wrapper);
    }

    /**
     * 反序列化JSON为消息
     */
    private ChatMessage deserializeMessage(String json) throws JsonProcessingException {
        MessageWrapper wrapper = objectMapper.readValue(json, MessageWrapper.class);
        
        return switch (wrapper.getType()) {
            case "SYSTEM" -> new SystemMessage(wrapper.getContent() != null ? wrapper.getContent() : "");
            case "USER" -> new UserMessage(wrapper.getContent() != null ? wrapper.getContent() : "");
            case "AI" -> {
                String content = wrapper.getContent();
                // 兼容旧数据：过滤历史遗留的 [tool-call] 占位符
                if (TOOL_CALL_PLACEHOLDER.equals(content)) {
                    content = null;
                }
                List<ToolCallInfo> toolCalls = wrapper.getToolCalls();

                if (toolCalls != null && !toolCalls.isEmpty()) {
                    List<ToolExecutionRequest> requests = new ArrayList<>();
                    for (ToolCallInfo tc : toolCalls) {
                        requests.add(ToolExecutionRequest.builder()
                            .id(tc.getId())
                            .name(tc.getName())
                            .arguments(tc.getArgs())
                            .build());
                    }
                    // 有正文时附带文本，无正文时只存工具调用结构
                    if (!isBlank(content)) {
                        yield AiMessage.from(content, requests);
                    } else {
                        yield AiMessage.from(requests);
                    }
                }

                if (isBlank(content)) {
                    yield null;
                }
                yield new AiMessage(content);
            }
            case "TOOL_EXECUTION_RESULT" -> {
                String content = wrapper.getContent();
                String toolCallId = wrapper.getToolCallId();
                String toolName = wrapper.getToolName();
                if (toolCallId != null && toolName != null) {
                    yield new ToolExecutionResultMessage(toolCallId, toolName, content != null ? content : "");
                }
                yield null;
            }
            default -> {
                log.warn("未知的消息类型: {}", wrapper.getType());
                yield null;
            }
        };
    }

    private String normalizeAiContent(String content) {
        if (isBlank(content)) {
            // 工具调用消息无正文时，不存占位符，避免被渲染到前端
            return null;
        }
        return content;
    }

    private boolean isBlank(String content) {
        return content == null || content.trim().isEmpty();
    }

    /**
     * 消息包装类（用于JSON序列化）
     */
    @lombok.Data
    public static class MessageWrapper {
        private String type;
        private String content;
        private Boolean hasToolCalls;
        private List<ToolCallInfo> toolCalls;
        private String toolCallId;
        private String toolName;
    }
    
    /**
     * 工具调用信息
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ToolCallInfo {
        private String id;
        private String name;
        private String args;
    }
}
