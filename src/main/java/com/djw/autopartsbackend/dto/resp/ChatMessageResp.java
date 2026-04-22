package com.djw.autopartsbackend.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 聊天消息响应
 *
 * @author dengjiawen
 * @since 2026-03-31
 */
@Data
@Schema(description = "聊天消息")
public class ChatMessageResp {

    @Schema(description = "角色：user/ai")
    private String role;

    @Schema(description = "消息内容")
    private String content;

    public static ChatMessageResp user(String content) {
        ChatMessageResp resp = new ChatMessageResp();
        resp.setRole("user");
        resp.setContent(content);
        return resp;
    }

    public static ChatMessageResp ai(String content) {
        ChatMessageResp resp = new ChatMessageResp();
        resp.setRole("ai");
        resp.setContent(content);
        return resp;
    }
}
