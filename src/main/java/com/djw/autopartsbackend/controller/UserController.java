package com.djw.autopartsbackend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.djw.autopartsbackend.common.PageResult;
import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.dto.LoginDTO;
import com.djw.autopartsbackend.entity.User;
import com.djw.autopartsbackend.security.JwtService;
import com.djw.autopartsbackend.security.RequireRole;
import com.djw.autopartsbackend.service.UserService;
import com.djw.autopartsbackend.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginDTO loginDTO) {
        User user = userService.login(loginDTO);
        String token = jwtService.generateToken(user.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", toUserVO(user));
        return Result.success(data);
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<UserVO> getCurrentUser(@RequestHeader(value = "token", required = false) String token) {
        if (!StringUtils.hasText(token)) {
            return Result.error(401, "未登录");
        }
        Long userId;
        try {
            userId = jwtService.parseUserId(token);
        } catch (Exception e) {
            return Result.error(401, "无效的token");
        }
        User user = userService.getById(userId);
        if (user == null) {
            return Result.error(401, "账号不存在或已禁用");
        }
        return Result.success(toUserVO(user));
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    @RequireRole({"ADMIN"})
    public Result<PageResult<UserVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Long roleId) {
        Page<User> pagination = new Page<>(page, pageSize);
        Page<User> result = userService.pageQuery(pagination, username, realName, roleId);
        List<UserVO> records = result.getRecords().stream().map(this::toUserVO).collect(Collectors.toList());
        return Result.success(PageResult.of(result.getTotal(), records));
    }

    @Operation(summary = "根据ID查询用户详情")
    @GetMapping("/{id}")
    @RequireRole({"ADMIN"})
    public Result<UserVO> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        return Result.success(toUserVO(user));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    @RequireRole({"ADMIN"})
    public Result<Void> add(@RequestBody User user) {
        if (userService.checkUsernameExists(user.getUsername(), null)) {
            return Result.error("用户名已存在");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            return Result.error("密码不能为空");
        }
        user.setPassword(encodeIfNeeded(user.getPassword()));
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
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(encodeIfNeeded(user.getPassword()));
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

    @Operation(summary = "修改当前用户密码")
    @PutMapping("/password")
    public Result<Void> changePassword(
            @RequestHeader(value = "token", required = false) String token,
            @RequestBody Map<String, String> passwordData) {
        if (!StringUtils.hasText(token)) {
            return Result.error(401, "未登录");
        }
        
        Long userId;
        try {
            userId = jwtService.parseUserId(token);
        } catch (Exception e) {
            return Result.error(401, "无效的 token");
        }
        
        String oldPassword = passwordData.get("oldPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (!StringUtils.hasText(oldPassword) || !StringUtils.hasText(newPassword)) {
            return Result.error(400, "密码不能为空");
        }
        
        if (newPassword.length() < 6) {
            return Result.error(400, "密码长度至少 6 位");
        }
        
        User user = userService.getById(userId);
        if (user == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error(400, "当前密码错误");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.updateById(user);
        
        return Result.success();
    }

    private String encodeIfNeeded(String password) {
        if (looksLikeBcrypt(password)) {
            return password;
        }
        return passwordEncoder.encode(password);
    }

    private boolean looksLikeBcrypt(String password) {
        return password.startsWith("$2a$") || password.startsWith("$2b$") || password.startsWith("$2y$");
    }

    private UserVO toUserVO(User user) {
        UserVO vo = new UserVO();
        if (user == null) {
            return vo;
        }
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}

