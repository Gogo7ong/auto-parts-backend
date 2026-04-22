package com.djw.autopartsbackend.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * AI对话请求
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Schema(description = "AI对话请求")
public class AiChatReq {

    @Schema(description = "用户消息", required = true)
    @NotBlank(message = "消息内容不能为空")
    private String message;

    @Schema(description = "会话ID（可选，用于多轮对话）")
    private String conversationId;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
