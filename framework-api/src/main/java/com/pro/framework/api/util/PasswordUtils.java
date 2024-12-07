package com.pro.framework.api.util;

import cn.hutool.crypto.digest.DigestUtil;

import java.security.MessageDigest;

/**
 * 默认密码生成器
 */
public class PasswordUtils {
    public static MessageDigest digest = null;
//
//    @SneakyThrows
//    public static String encrypt_Password(String rawPassword) {
//        if (rawPassword == null) {
//            return null;
//        }
//        if (digest == null) {
//            digest = MessageDigest.getInstance("SHA-256");
//        }
//        // SHA-256算法 将原始密码转换为字节数组，并进行哈希计算 使用Base64编码将字节数组转换为字符串
//        return Base64.getEncoder().encodeToString(digest.digest(rawPassword.getBytes()));
//    }

    /**
     * 密码加密
     *
     * @param password 原文密码
     * @return 密文密码
     */
    public static String encrypt_Password(String password) {
        return null == password ? null : DigestUtil.md5Hex(password + "!@#k253&^*").toUpperCase();
    }

    public static String encrypt_tkPassword(String password) {
        return null == password ? null : DigestUtil.md5Hex(password + "k983!@#&^*").toUpperCase();
    }
}
