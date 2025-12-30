-- 添加邮件模板配置和平台名称配置
-- 用于密码重置邮件等场景

-- 密码重置邮件主题模板
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.password-reset.subject', '密码重置验证码 - 趣爱巢', '密码重置邮件主题', 
 '密码重置邮件主题模板。支持变量：{username}（用户名）、{platform}（平台名称）', 
 'text', 14, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 密码重置邮件正文模板
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.password-reset.content', 
'尊敬的 {username} 用户：

您申请了密码重置，请使用以下验证码重置您的密码：

验证码：{resetCode}

或者点击以下链接直接重置密码：
{resetUrl}

此验证码有效期为{expireMinutes}分钟，请及时操作。
如果您没有申请密码重置，请忽略此邮件。

{platform}', 
 '密码重置邮件正文模板', 
 '密码重置邮件正文模板。支持变量：{username}（用户名）、{resetCode}（验证码）、{resetUrl}（重置链接）、{expireMinutes}（有效期分钟数）、{platform}（平台名称）', 
 'textarea', 15, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 平台名称配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('app.platform.name', 'B2B采购平台', '平台名称', 
 '平台名称，用于邮件模板等场景', 
 'text', 31, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` IN ('mail.password-reset.subject', 'mail.password-reset.content', 'app.platform.name');













