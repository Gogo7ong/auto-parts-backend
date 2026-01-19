package com.djw.autopartsbackend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    @Schema(description = "用户名", required = true)
    private String username;

    @Schema(description = "密码", required = true)
    private String password;
}
