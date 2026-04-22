package com.djw.autopartsbackend.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI对话响应
 *
 * @author dengjiawen
 * @since 2026-01-18
 */
@Schema(description = "AI对话响应")
public class AiChatResp {

    @Schema(description = "AI回答内容")
    private String answer;

    @Schema(description = "会话ID")
    private String conversationId;

    public AiChatResp() {
    }

    public AiChatResp(String answer, String conversationId) {
        this.answer = answer;
        this.conversationId = conversationId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
