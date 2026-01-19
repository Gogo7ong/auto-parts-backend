package com.djw.autopartsbackend.controller;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.LoginDTO;
import com.djw.autopartsbackend.entity.User;
import com.djw.autopartsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengjiawen
 * @since 2025-01-19
 */
@Tag(name = "认证管理", description = "用户认证接口")
@RestController
@RequestMapping("/api/user")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        Map<String, Object> data = new HashMap<>();
        data.put("token", "token-" + user.getId());
        data.put("user", user);
        return Result.success(data);
    }
}
