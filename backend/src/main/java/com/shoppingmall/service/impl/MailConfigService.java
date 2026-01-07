package com.shoppingmall.service.impl;

import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

import java.util.Properties;

/**
 * 邮件配置服务（从数据库读取配置）
 * 动态创建 JavaMailSender，确保每次发送邮件时使用最新配置
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MailConfigService {
    
    private final SystemConfigService systemConfigService;
    
    /**
     * 动态创建 JavaMailSender（每次发送邮件时从数据库读取最新配置）
     *
     * @return JavaMailSender 实例
     */
    public JavaMailSender createMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        
        // 从数据库读取邮件配置
        String host = systemConfigService.getConfigValue("mail.host", "smtp.qq.com");
        String portStr = systemConfigService.getConfigValue("mail.port", "587");
        String username = systemConfigService.getConfigValue("mail.username");
        String password = systemConfigService.getConfigValue("mail.password");
        
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("邮件配置不完整，请在系统配置中设置发件人邮箱（mail.username）");
        }
        
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("邮件配置不完整，请在系统配置中设置邮箱授权码（mail.password）");
        }
        
        try {
            mailSender.setHost(host);
            mailSender.setPort(Integer.parseInt(portStr));
            mailSender.setUsername(username);
            mailSender.setPassword(password);
            mailSender.setDefaultEncoding("UTF-8");
            
            // 设置SMTP属性
            Properties props = mailSender.getJavaMailProperties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.starttls.required", "true");
            props.put("mail.smtp.connectiontimeout", "5000");
            props.put("mail.smtp.timeout", "5000");
            props.put("mail.smtp.writetimeout", "5000");
            
            log.debug("创建邮件发送器成功: host={}, port={}, username={}", host, portStr, username);
            return mailSender;
        } catch (NumberFormatException e) {
            log.error("邮件服务器端口配置格式错误: {}", portStr, e);
            throw new RuntimeException("邮件服务器端口配置格式错误，必须是数字");
        }
    }
    
    /**
     * 获取发件人邮箱地址
     *
     * @return 发件人邮箱地址
     */
    public String getFromEmail() {
        String username = systemConfigService.getConfigValue("mail.username");
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("邮件配置不完整，请在系统配置中设置发件人邮箱（mail.username）");
        }
        return username;
    }
}



























