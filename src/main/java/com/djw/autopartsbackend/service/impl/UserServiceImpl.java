package com.djw.autopartsbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.djw.autopartsbackend.dto.LoginDTO;
import com.djw.autopartsbackend.entity.User;
import com.djw.autopartsbackend.mapper.UserMapper;
import com.djw.autopartsbackend.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return this.getOne(wrapper);
    }

    @Override
    public boolean checkUsernameExists(String username, Long excludeId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        if (excludeId != null) {
            wrapper.ne(User::getId, excludeId);
        }
        return this.count(wrapper) > 0;
    }

    @Override
    public Page<User> pageQuery(Page<User> page, String username, String realName, Long roleId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(username), User::getUsername, username)
                .like(StringUtils.hasText(realName), User::getRealName, realName)
                .eq(roleId != null, User::getRoleId, roleId)
                .orderByDesc(User::getCreateTime);
        return this.page(page, wrapper);
    }

    @Override
    public User login(LoginDTO loginDTO) {
        User user = getByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }
        return user;
    }
}
