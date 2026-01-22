package com.djw.autopartsbackend.security;

import com.djw.autopartsbackend.common.Result;
import com.djw.autopartsbackend.entity.Role;
import com.djw.autopartsbackend.entity.User;
import com.djw.autopartsbackend.mapper.RoleMapper;
import com.djw.autopartsbackend.mapper.UserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Arrays;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(UserMapper userMapper, RoleMapper roleMapper, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.roleMapper = roleMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole == null) {
            return true;
        }

        String token = request.getHeader("token");
        if (!StringUtils.hasText(token) || !token.startsWith("token-")) {
            writeJson(response, Result.error(401, "未登录"));
            return false;
        }

        Long userId;
        try {
            userId = Long.parseLong(token.substring("token-".length()));
        } catch (NumberFormatException ex) {
            writeJson(response, Result.error(401, "无效的token"));
            return false;
        }

        User user = userMapper.selectById(userId);
        if (user == null || user.getStatus() == null || user.getStatus() == 0) {
            writeJson(response, Result.error(401, "账号不存在或已禁用"));
            return false;
        }

        Role role = roleMapper.selectById(user.getRoleId());
        if (role == null || !Arrays.asList(requireRole.value()).contains(role.getRoleCode())) {
            writeJson(response, Result.error(403, "无权限"));
            return false;
        }

        return true;
    }

    private void writeJson(HttpServletResponse response, Result<?> result) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
