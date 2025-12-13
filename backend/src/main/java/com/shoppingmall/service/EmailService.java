package com.shoppingmall.service;

/**
 * 邮件服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
public interface EmailService {
    /**
     * 发送密码重置邮件
     *
     * @param to 收件人邮箱
     * @param username 用户名
     * @param resetCode 重置验证码
     * @param expireMinutes 过期时间（分钟）
     * @return 发送的邮件内容
     */
    String sendPasswordResetEmail(String to, String username, String resetCode, int expireMinutes);
}

