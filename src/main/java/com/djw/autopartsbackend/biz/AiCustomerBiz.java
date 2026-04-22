package com.djw.autopartsbackend.biz;

import com.djw.autopartsbackend.dto.req.AiChatReq;
import com.djw.autopartsbackend.dto.resp.AiChatResp;
import com.djw.autopartsbackend.service.AiCustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.UUID;

/**
 * AI智能客服业务层
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiCustomerBiz {

    private final AiCustomerService aiCustomerService;

    /**
     * 处理AI对话请求
     *
     * @param req 对话请求
     * @return 对话响应
     */
    public AiChatResp chat(AiChatReq req) {
        log.info("AI对话请求: conversationId={}, message={}", req.getConversationId(), req.getMessage());
        
        // 如果没有会话ID，生成一个新的
        String conversationId = req.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }
        
        // 调用服务层，支持会话记忆
        String answer = aiCustomerService.chatWithHistory(req.getMessage(), conversationId);
        
        log.info("AI对话响应: conversationId={}, answer长度={}", conversationId, answer.length());
        return new AiChatResp(answer, conversationId);
    }

    /**
     * 处理AI流式对话请求（SSE）
     *
     * @param req 对话请求
     * @return SSE发射器
     */
    public SseEmitter chatStream(AiChatReq req) {
        log.info("AI流式对话请求: conversationId={}, message={}", req.getConversationId(), req.getMessage());

        // 如果没有会话ID，生成一个新的
        String conversationId = req.getConversationId();
        if (conversationId == null || conversationId.isEmpty()) {
            conversationId = UUID.randomUUID().toString();
        }

        // 创建SSE发射器，超时时间5分钟
        SseEmitter emitter = new SseEmitter(5 * 60 * 1000L);
        final String finalConversationId = conversationId;

        // 设置超时回调
        emitter.onTimeout(() -> {
            log.warn("SSE连接超时: conversationId={}", finalConversationId);
            emitter.complete();
        });

        // 设置错误回调
        emitter.onError(error -> {
            log.error("SSE连接错误: conversationId={}, error={}", finalConversationId, error.getMessage());
        });

        // 设置完成回调
        emitter.onCompletion(() -> {
            log.info("SSE连接完成: conversationId={}", finalConversationId);
        });

        // 调用服务层流式对话
        aiCustomerService.chatStreamWithHistory(
                req.getMessage(),
                finalConversationId,
                // onNext: 每收到一个token就发送
                token -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("message")
                                .data(token));
                    } catch (IOException e) {
                        log.error("发送SSE消息失败: {}", e.getMessage());
                        emitter.complete();
                    }
                },
                // onComplete: 完成时发送会话ID并关闭连接
                () -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("done")
                                .data(finalConversationId));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送SSE完成消息失败: {}", e.getMessage());
                        emitter.complete();
                    }
                },
                // onError: 错误时发送错误消息
                error -> {
                    try {
                        emitter.send(SseEmitter.event()
                                .name("error")
                                .data(error.getMessage()));
                        emitter.complete();
                    } catch (IOException e) {
                        log.error("发送SSE错误消息失败: {}", e.getMessage());
                        emitter.complete();
                    }
                }
        );

        return emitter;
    }
}
