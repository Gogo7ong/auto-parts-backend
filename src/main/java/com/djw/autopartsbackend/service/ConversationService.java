package com.djw.autopartsbackend.service;

import com.djw.autopartsbackend.dto.resp.ChatMessageResp;
import com.djw.autopartsbackend.dto.resp.ConversationResp;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 会话历史服务
 * 管理AI对话会话的查询和删除
 *
 * @author dengjiawen
 * @since 2026-03-31
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KEY_PREFIX = "ai:chat:memory:";

    /**
     * 获取所有会话列表
     *
     * @return 会话列表
     */
    public List<ConversationResp> getConversationList() {
        Set<String> keys = redisTemplate.keys(KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<ConversationResp> conversations = new ArrayList<>();
        for (String key : keys) {
            try {
                String conversationId = key.replace(KEY_PREFIX, "");
                ConversationResp resp = getConversationInfo(conversationId);
                if (resp != null && resp.getMessageCount() > 0) {
                    conversations.add(resp);
                }
            } catch (Exception e) {
                log.warn("获取会话信息失败: {}", e.getMessage());
            }
        }

        // 按更新时间倒序排列
        conversations.sort((a, b) -> b.getUpdateTime().compareTo(a.getUpdateTime()));
        return conversations;
    }

    /**
     * 获取会话详情（消息列表）
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    public List<ChatMessageResp> getConversationMessages(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        List<String> jsonList = redisTemplate.opsForList().range(key, 0, -1);

        if (jsonList == null || jsonList.isEmpty()) {
            return new ArrayList<>();
        }

        List<ChatMessageResp> messages = new ArrayList<>();
        for (String json : jsonList) {
            try {
                Map<String, Object> wrapper = objectMapper.readValue(json, Map.class);
                String type = (String) wrapper.get("type");
                String content = (String) wrapper.get("content");

                if ("USER".equals(type)) {
                    messages.add(ChatMessageResp.user(content));
                } else if ("AI".equals(type)) {
                    messages.add(ChatMessageResp.ai(content));
                }
                // SYSTEM消息不返回给前端
            } catch (Exception e) {
                log.warn("解析消息失败: {}", e.getMessage());
            }
        }

        return messages;
    }

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @return 是否成功
     */
    public boolean deleteConversation(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        Boolean deleted = redisTemplate.delete(key);
        log.info("删除会话: conversationId={}, 结果={}", conversationId, deleted);
        return Boolean.TRUE.equals(deleted);
    }

    /**
     * 获取会话基本信息
     */
    private ConversationResp getConversationInfo(String conversationId) {
        String key = KEY_PREFIX + conversationId;
        Long size = redisTemplate.opsForList().size(key);

        if (size == null || size == 0) {
            return null;
        }

        // 获取最后一条消息
        String lastJson = redisTemplate.opsForList().index(key, -1);
        String lastMessage = "";
        if (lastJson != null) {
            try {
                Map<String, Object> wrapper = objectMapper.readValue(lastJson, Map.class);
                String content = (String) wrapper.get("content");
                if (content != null && content.length() > 50) {
                    lastMessage = content.substring(0, 50) + "...";
                } else {
                    lastMessage = content;
                }
            } catch (Exception e) {
                log.warn("解析最后消息失败: {}", e.getMessage());
            }
        }

        // 获取key的过期时间作为更新时间
        Long ttl = redisTemplate.getExpire(key);
        LocalDateTime updateTime = LocalDateTime.now();
        if (ttl != null && ttl > 0) {
            // 估算更新时间（过期时间剩余越多，说明越新）
            updateTime = LocalDateTime.now().minusSeconds(7 * 24 * 3600 - ttl);
        }

        ConversationResp resp = new ConversationResp();
        resp.setConversationId(conversationId);
        resp.setLastMessage(lastMessage);
        resp.setMessageCount(size.intValue());
        resp.setUpdateTime(updateTime);
        return resp;
    }
}
