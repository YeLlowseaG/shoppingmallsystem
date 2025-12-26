-- ============================================
-- 添加支付宝网关地址配置
-- 创建时间：2025-12-26
-- 说明：将支付宝网关地址配置化，支持在管理后台修改
-- ============================================

USE chengren_shopping_mall;

-- 添加支付宝沙箱和生产环境网关地址配置
INSERT INTO system_config (config_key, config_value, config_name, config_desc, config_type, sort_order, status, create_time, update_time) VALUES
('payment.alipay.sandbox.gateway', 'https://openapi-sandbox.dl.alipaydev.com/gateway.do', '支付宝沙箱网关地址', '支付宝沙箱环境网关地址', 'text', 22, 1, NOW(), NOW()),
('payment.alipay.production.gateway', 'https://openapi.alipay.com/gateway.do', '支付宝生产网关地址', '支付宝生产环境网关地址', 'text', 25, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_value = VALUES(config_value),
    config_name = VALUES(config_name),
    config_desc = VALUES(config_desc),
    config_type = VALUES(config_type),
    sort_order = VALUES(sort_order),
    update_time = NOW();

-- 验证更新结果
SELECT
    config_key,
    config_value,
    config_name,
    update_time
FROM system_config
WHERE config_key IN ('payment.alipay.sandbox.gateway', 'payment.alipay.production.gateway')
ORDER BY sort_order;