package com.shoppingmall.util;

import com.shoppingmall.common.util.EncryptUtil;

/**
 * 生成BCrypt密码哈希的工具类
 * 用于生成测试数据的密码哈希值
 */
public class GenerateBcryptHash {
    public static void main(String[] args) {
        String password = "123456";
        String hash = EncryptUtil.bcryptEncode(password);
        System.out.println("密码: " + password);
        System.out.println("BCrypt Hash: " + hash);
        System.out.println();
        System.out.println("验证测试:");
        boolean matches = EncryptUtil.bcryptMatches(password, hash);
        System.out.println("密码验证结果: " + matches);
    }
}






