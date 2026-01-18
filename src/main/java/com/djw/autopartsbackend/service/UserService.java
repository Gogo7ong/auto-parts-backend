package com.djw.autopartsbackend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.djw.autopartsbackend.entity.User;

/**
 * @author dengjiawen
 * @since 2025-01-18
 */
public interface UserService extends IService<User> {

    User getByUsername(String username);

    boolean checkUsernameExists(String username, Long excludeId);

    Page<User> pageQuery(Page<User> page, String username, String realName, Long roleId);
}
