package com.shoppingmall.service.impl;

import com.shoppingmall.service.EmailService;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

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

    private final MailConfigService mailConfigService;
    private final SystemConfigService systemConfigService;

    /**
     * 获取发件人邮箱（从数据库配置读取）
     */
    private String getFromEmail() {
        return mailConfigService.getFromEmail();
    }

    /**
     * 获取前端地址（从数据库配置读取）
     */
    private String getFrontendUrl() {
        return systemConfigService.getConfigValue("app.frontend.url", "http://localhost:3002");
    }

    /**
     * 获取平台名称（从数据库配置读取）
     */
    private String getPlatformName() {
        return systemConfigService.getConfigValue("app.platform.name", "B2B采购平台");
    }

    /**
     * 替换模板变量
     *
     * @param template 模板字符串
     * @param variables 变量映射
     * @return 替换后的字符串
     */
    private String replaceTemplateVariables(String template, Map<String, String> variables) {
        if (template == null || template.isEmpty()) {
            return template;
        }
        
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            String value = entry.getValue() != null ? entry.getValue() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }

    /**
     * 获取密码重置邮件主题模板
     */
    private String getPasswordResetSubjectTemplate() {
        String template = systemConfigService.getConfigValue("mail.password-reset.subject");
        if (template == null || template.trim().isEmpty()) {
            // 默认模板
            return "密码重置验证码 - B2B采购平台";
        }
        return template;
    }

    /**
     * 获取密码重置邮件正文模板
     */
    private String getPasswordResetContentTemplate() {
        String template = systemConfigService.getConfigValue("mail.password-reset.content");
        if (template == null || template.trim().isEmpty()) {
            // 默认模板
            return "尊敬的 {username} 用户：\n\n" +
                   "您申请了密码重置，请使用以下验证码重置您的密码：\n\n" +
                   "验证码：{resetCode}\n\n" +
                   "或者点击以下链接直接重置密码：\n" +
                   "{resetUrl}\n\n" +
                   "此验证码有效期为{expireMinutes}分钟，请及时操作。\n" +
                   "如果您没有申请密码重置，请忽略此邮件。\n\n" +
                   "{platform}";
        }
        return template;
    }

    @Override
    public String sendPasswordResetEmail(String to, String username, String resetCode, int expireMinutes) {
        try {
            // 动态创建邮件发送器（使用最新配置）
            JavaMailSender mailSender = mailConfigService.createMailSender();
            
            // 获取配置
            String fromEmail = getFromEmail();
            String frontendUrl = getFrontendUrl();
            String platformName = getPlatformName();
            
            // 构建重置链接
            String resetUrl = frontendUrl + "/reset-password?code=" + resetCode;
            
            // 准备模板变量
            Map<String, String> variables = new HashMap<>();
            variables.put("username", username);
            variables.put("resetCode", resetCode);
            variables.put("resetUrl", resetUrl);
            variables.put("expireMinutes", String.valueOf(expireMinutes));
            variables.put("platform", platformName);
            
            // 获取并替换邮件主题模板
            String subjectTemplate = getPasswordResetSubjectTemplate();
            String subject = replaceTemplateVariables(subjectTemplate, variables);
            
            // 获取并替换邮件正文模板
            String contentTemplate = getPasswordResetContentTemplate();
            String content = replaceTemplateVariables(contentTemplate, variables);
            
            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
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

