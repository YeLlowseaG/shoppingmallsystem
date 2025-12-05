package com.shoppingmall.common.util;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.digest.BCrypt;

/**
 * 加密工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
public class EncryptUtil {

    /**
     * BCrypt加密密码
     *
     * @param password 明文密码
     * @return 加密后的密码
     */
    public static String bcryptEncode(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    /**
     * BCrypt验证密码
     *
     * @param password       明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean bcryptMatches(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }

    /**
     * MD5加密
     *
     * @param data 数据
     * @return 加密后的字符串
     */
    public static String md5(String data) {
        return SecureUtil.md5(data);
    }

    /**
     * SHA256加密
     *
     * @param data 数据
     * @return 加密后的字符串
     */
    public static String sha256(String data) {
        return SecureUtil.sha256(data);
    }

    /**
     * AES加密
     *
     * @param data 数据
     * @param key  密钥
     * @return 加密后的字符串
     */
    public static String aesEncrypt(String data, String key) {
        return SecureUtil.aes(key.getBytes()).encryptBase64(data);
    }

    /**
     * AES解密
     *
     * @param encryptedData 加密后的数据
     * @param key           密钥
     * @return 解密后的字符串
     */
    public static String aesDecrypt(String encryptedData, String key) {
        return SecureUtil.aes(key.getBytes()).decryptStr(encryptedData);
    }
}

