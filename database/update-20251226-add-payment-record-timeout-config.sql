-- 添加支付记录自动取消时间配置
-- 用于配置支付中状态的支付记录自动关闭时间，支持在管理后台动态修改

-- 支付记录自动取消时间
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('payment.record-timeout-hours', '2', '支付记录自动取消时间', '支付中状态的支付记录自动关闭时间（小时），默认2小时。超过此时间仍未完成支付的记录将自动关闭。', 'number', 21, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` = 'payment.record-timeout-hours';

