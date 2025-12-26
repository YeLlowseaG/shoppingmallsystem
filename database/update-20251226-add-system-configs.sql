-- 添加系统配置（邮箱配置、订单配置、应用配置）
-- 用于系统配置管理，支持在管理后台动态修改

-- ==================== 邮箱配置 ====================
-- 邮件服务器地址
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.host', 'smtp.qq.com', '邮件服务器地址', 'SMTP邮件服务器地址，如：smtp.qq.com、smtp.163.com等', 'text', 10, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 邮件服务器端口
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.port', '587', '邮件服务器端口', 'SMTP邮件服务器端口，QQ邮箱通常为587，163邮箱为25或465', 'number', 11, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 发件人邮箱
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.username', '501360872@qq.com', '发件人邮箱', '用于发送邮件的邮箱地址', 'text', 12, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 邮箱授权码
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('mail.password', 'bokvnbgivdjwbjhb', '邮箱授权码', '邮箱授权码（不是登录密码），QQ邮箱需要在设置中开启SMTP服务并生成授权码', 'text', 13, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- ==================== 订单配置 ====================
-- 订单支付超时时间
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('order.payment-timeout-hours', '4', '订单支付超时时间', '待付款订单自动取消时间（小时），默认4小时。B2B平台建议4-6小时，给企业用户充足的决策和审批时间。', 'number', 20, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- ==================== 应用配置 ====================
-- 密码重置令牌有效期（如果已存在则更新描述）
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('app.password.reset.token-expire-minutes', '30', '密码重置令牌有效期', '密码重置令牌有效期（分钟），默认30分钟。支持动态修改，修改后立即生效。', 'number', 30, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` IN (
    'mail.host', 
    'mail.port', 
    'mail.username', 
    'mail.password',
    'order.payment-timeout-hours',
    'app.password.reset.token-expire-minutes'
) ORDER BY `sort_order`;

