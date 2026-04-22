package com.djw.autopartsbackend.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话信息响应
 *
 * @author dengjiawen
 * @since 2026-03-31
 */
@Data
@Schema(description = "会话信息")
public class ConversationResp {

    @Schema(description = "会话ID")
    private String conversationId;

    @Schema(description = "最后一条消息")
    private String lastMessage;

    @Schema(description = "消息数量")
    private Integer messageCount;

    @Schema(description = "最后更新时间")
    private LocalDateTime updateTime;
}
