-- 添加前端地址配置
-- 用于支付回调跳转等场景
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `sort_order`, `status`) 
VALUES ('app.frontend.url', 'http://localhost:3002', '前端地址', '前端应用访问地址，用于支付回调跳转等场景。支持动态修改，修改后立即生效，无需重启服务。', 'text', 100, 1)
ON DUPLICATE KEY UPDATE 
    `config_value` = VALUES(`config_value`),
    `config_name` = VALUES(`config_name`),
    `config_desc` = VALUES(`config_desc`),
    `config_type` = VALUES(`config_type`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`);

-- 查询验证
SELECT * FROM `system_config` WHERE `config_key` = 'app.frontend.url';

