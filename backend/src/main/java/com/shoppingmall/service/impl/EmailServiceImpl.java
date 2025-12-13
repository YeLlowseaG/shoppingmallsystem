package com.shoppingmall.service.impl;

import com.shoppingmall.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * 邮件服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3002}")
    private String frontendUrl;

    @Override
    public String sendPasswordResetEmail(String to, String username, String resetCode, int expireMinutes) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("密码重置验证码 - B2B采购平台");

            String resetUrl = frontendUrl + "/reset-password?code=" + resetCode;
            String content = String.format(
                "尊敬的 %s 用户：\n\n" +
                "您申请了密码重置，请使用以下验证码重置您的密码：\n\n" +
                "验证码：%s\n\n" +
                "或者点击以下链接直接重置密码：\n" +
                "%s\n\n" +
                "此验证码有效期为%d分钟，请及时操作。\n" +
                "如果您没有申请密码重置，请忽略此邮件。\n\n" +
                "B2B采购平台",
                username, resetCode, resetUrl, expireMinutes
            );

            message.setText(content);
            mailSender.send(message);
            log.info("密码重置邮件发送成功: to={}, username={}, code={}", to, username, resetCode);
            return content;
        } catch (Exception e) {
            log.error("发送密码重置邮件失败: to={}, username={}", to, username, e);
            throw new RuntimeException("邮件发送失败，请稍后重试");
        }
    }
}

