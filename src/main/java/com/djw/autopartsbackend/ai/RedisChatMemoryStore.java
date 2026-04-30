package com.djw.autopartsbackend.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisChatMemoryStore implements ChatMemoryStore {

    private static final String KEY_PREFIX = "ai:chat:memory:";
    private static final String TOOL_CALL_PLACEHOLDER = "[tool-call]";
    private static final Duration SESSION_TTL = Duration.ofDays(7);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        redisTemplate.delete(key);

        if (messages == null || messages.isEmpty()) {
            return;
        }

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

    @Override
    public void deleteMessages(Object memoryId) {
        String key = getKey(memoryId);
        redisTemplate.delete(key);
        log.debug("删除会话记忆: memoryId={}", memoryId);
    }

    private String getKey(Object memoryId) {
        return KEY_PREFIX + memoryId;
    }

    private String serializeMessage(ChatMessage message) throws JsonProcessingException {
        MessageWrapper wrapper = new MessageWrapper();
        wrapper.setType(message.type().name());

        if (message instanceof SystemMessage systemMessage) {
            wrapper.setContent(systemMessage.text());
        } else if (message instanceof UserMessage userMessage) {
            wrapper.setContent(userMessage.singleText());
        } else if (message instanceof AiMessage aiMessage) {
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
        } else if (message instanceof ToolExecutionResultMessage toolResult) {
            wrapper.setContent(toolResult.text());
            wrapper.setToolCallId(toolResult.id());
            wrapper.setToolName(toolResult.toolName());
        } else {
            log.debug("未处理的消息类型: {}", message.type());
            return null;
        }

        return objectMapper.writeValueAsString(wrapper);
    }

    private ChatMessage deserializeMessage(String json) throws JsonProcessingException {
        MessageWrapper wrapper = objectMapper.readValue(json, MessageWrapper.class);
        String type = wrapper.getType();

        if ("SYSTEM".equals(type)) {
            return new SystemMessage(defaultText(wrapper.getContent()));
        }
        if ("USER".equals(type)) {
            return new UserMessage(defaultText(wrapper.getContent()));
        }
        if ("AI".equals(type)) {
            return deserializeAiMessage(wrapper);
        }
        if ("TOOL_EXECUTION_RESULT".equals(type)) {
            String toolCallId = wrapper.getToolCallId();
            String toolName = wrapper.getToolName();
            if (toolCallId != null && toolName != null) {
                return new ToolExecutionResultMessage(toolCallId, toolName, defaultText(wrapper.getContent()));
            }
            return null;
        }

        log.warn("未知的消息类型: {}", type);
        return null;
    }

    private ChatMessage deserializeAiMessage(MessageWrapper wrapper) {
        String content = wrapper.getContent();
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
            if (!isBlank(content)) {
                return AiMessage.from(content, requests);
            }
            return AiMessage.from(requests);
        }

        if (isBlank(content)) {
            return null;
        }
        return new AiMessage(content);
    }

    private String normalizeAiContent(String content) {
        return isBlank(content) ? null : content;
    }

    private String defaultText(String content) {
        return content != null ? content : "";
    }

    private boolean isBlank(String content) {
        return content == null || content.trim().isEmpty();
    }

    @lombok.Data
    public static class MessageWrapper {
        private String type;
        private String content;
        private Boolean hasToolCalls;
        private List<ToolCallInfo> toolCalls;
        private String toolCallId;
        private String toolName;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ToolCallInfo {
        private String id;
        private String name;
        private String args;
    }
}
