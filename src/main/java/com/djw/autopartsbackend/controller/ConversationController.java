package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.resp.ChatMessageResp;
import com.djw.autopartsbackend.dto.resp.ConversationResp;
import com.djw.autopartsbackend.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 会话历史控制器
 *
 * @author dengjiawen
 * @since 2026-03-31
 */
@Slf4j
@RestController
@RequestMapping("/api/conversations")
@Tag(name = "会话历史", description = "AI对话会话管理接口")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    /**
     * 获取所有会话列表
     *
     * @return 会话列表
     */
    @GetMapping
    @Operation(summary = "获取会话列表", description = "获取用户的所有AI对话会话")
    public Result<List<ConversationResp>> getConversationList() {
        List<ConversationResp> list = conversationService.getConversationList();
        return Result.success(list);
    }

    /**
     * 获取会话消息详情
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @GetMapping("/{conversationId}/messages")
    @Operation(summary = "获取会话消息", description = "获取指定会话的所有消息")
    public Result<List<ChatMessageResp>> getConversationMessages(@PathVariable String conversationId) {
        List<ChatMessageResp> messages = conversationService.getConversationMessages(conversationId);
        return Result.success(messages);
    }

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}")
    @Operation(summary = "删除会话", description = "删除指定的对话会话")
    public Result<Boolean> deleteConversation(@PathVariable String conversationId) {
        boolean success = conversationService.deleteConversation(conversationId);
        return Result.success(success);
    }

    /**
     * 清理指定会话的记忆（修复异常数据）
     *
     * @param conversationId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/{conversationId}/memory")
    @Operation(summary = "清理会话记忆", description = "清理指定会话的Redis记忆数据（用于修复异常）")
    public Result<Boolean> clearConversationMemory(@PathVariable String conversationId) {
        boolean success = conversationService.deleteConversation(conversationId);
        log.info("清理会话记忆: conversationId={}, 结果={}", conversationId, success);
        return Result.success(success);
    }
}
