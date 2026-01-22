package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.LoginDTO;
import com.djw.autopartsbackend.entity.User;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Tag(name = "用户管理", description = "用户管理接口")
@RestController
@RequestMapping("/api/users")
public class UserController {

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

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> getCurrentUser(@RequestHeader(value = "token", required = false) String token) {
        if (token == null || token.isEmpty()) {
            return Result.error("未登录");
        }
        try {
            Long userId = Long.parseLong(token.replace("token-", ""));
            User user = userService.getById(userId);
            return Result.success(user);
        } catch (NumberFormatException e) {
            return Result.error("无效的token");
        }
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    @RequireRole({"ADMIN"})
    public Result<PageResult<User>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId) {
        Page<User> pagination = new Page<>(page, pageSize);
        Page<User> result = userService.pageQuery(pagination, username, realName, roleId);
        return Result.success(PageResult.of(result.getTotal(), result.getRecords()));
    }

    @Operation(summary = "根据ID查询用户详情")
    @GetMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<Void> add(@RequestBody User user) {
        if (userService.checkUsernameExists(user.getUsername(), null)) {
            return Result.error("用户名已存在");
        }
        userService.save(user);
        return Result.success();
    }

    @Operation(summary = "更新用户信息")
    @PutMapping
    @RequireRole({"ADMIN"})
    public Result<Void> update(@RequestBody User user) {
        if (userService.checkUsernameExists(user.getUsername(), user.getId())) {
            return Result.error("用户名已存在");
        }
        userService.updateById(user);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<Void> delete(@PathVariable Long id) {
        userService.removeById(id);
        return Result.success();
    }
}
