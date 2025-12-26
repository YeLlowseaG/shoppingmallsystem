-- 添加预存款记录自动取消时间配置
-- 用于配置支付中状态的预存款记录自动超时时间，支持在管理后台动态修改

-- 预存款记录自动取消时间
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`)
VALUES 
('deposit.record-timeout-hours', '1', '预存款记录自动取消时间', '支付中状态的预存款记录自动超时时间（小时），默认1小时。超过此时间仍未完成支付的记录将自动更新为已超时状态。', 'number', 22, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` = 'deposit.record-timeout-hours';

