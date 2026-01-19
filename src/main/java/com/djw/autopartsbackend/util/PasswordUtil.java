package com.djw.autopartsbackend.util;

import cn.hutool.crypto.digest.BCrypt;

public class PasswordUtil {
    public static void main(String[] args) {
        String password = "123456";
        String hashedPassword = BCrypt.hashpw(password);
        System.out.println("密码: " + password);
        System.out.println("BCrypt 哈希值: " + hashedPassword);
        
        boolean matches = BCrypt.checkpw(password, hashedPassword);
        System.out.println("验证结果: " + matches);
    }
}
