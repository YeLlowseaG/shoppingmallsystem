-- 添加企业微信通知配置
-- 用于在用户下单、支付成功时发送通知到企业微信群
-- 使用企业微信群机器人 Webhook 功能

-- ==================== 企业微信通知配置 ====================
-- 企业微信群机器人 Webhook URL
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES
('wechat.work.webhook.url', '', '企业微信群机器人Webhook URL', '企业微信群机器人的Webhook地址，用于发送订单通知。在企业微信群中添加机器人后获取。格式：https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=xxxxx', 'textarea', 100, 1)
ON DUPLICATE KEY UPDATE
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 企业微信通知开关
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES
('wechat.work.notification.enabled', '1', '企业微信通知开关', '是否启用企业微信通知功能。启用后，用户下单和支付成功时会发送通知到企业微信群。1=启用，0=禁用', 'number', 101, 1)
ON DUPLICATE KEY UPDATE
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` IN (
    'wechat.work.webhook.url',
    'wechat.work.notification.enabled'
) ORDER BY `sort_order`;
