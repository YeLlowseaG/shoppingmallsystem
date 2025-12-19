package com.shoppingmall.util;

import cn.hutool.crypto.digest.BCrypt;

public class BcryptTest {
    public static void main(String[] args) {
        String password = "123456";
        // 生成BCrypt hash
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println("Password: " + password);
        System.out.println("BCrypt Hash: " + hash);
        
        // 验证
        boolean matches = BCrypt.checkpw(password, hash);
        System.out.println("Verification: " + matches);
    }
}







































