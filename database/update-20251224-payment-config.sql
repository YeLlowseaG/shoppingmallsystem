-- ============================================
-- 第三方支付配置初始化脚本
-- 创建时间：2025-12-24
-- 说明：初始化微信支付和支付宝支付配置项到system_config表
-- ============================================

-- 微信支付配置初始化
INSERT INTO system_config (config_key, config_value, config_name, config_desc, config_type, sort_order, status, create_time, update_time) VALUES
('payment.wechat.enabled', '1', '微信支付开关', '是否启用微信支付：1-启用，0-禁用', 'text', 1, 1, NOW(), NOW()),
('payment.wechat.env', 'sandbox', '微信支付环境', '支付环境：sandbox-沙箱，production-生产', 'text', 2, 1, NOW(), NOW()),
('payment.wechat.sandbox.appid', '', '微信沙箱AppID', '微信支付沙箱环境AppID', 'text', 3, 1, NOW(), NOW()),
('payment.wechat.sandbox.mchid', '', '微信沙箱商户号', '微信支付沙箱环境商户号', 'text', 4, 1, NOW(), NOW()),
('payment.wechat.sandbox.key', '', '微信沙箱API密钥', '微信支付沙箱环境API密钥（敏感信息，请妥善保管）', 'textarea', 5, 1, NOW(), NOW()),
('payment.wechat.sandbox.cert_path', '', '微信沙箱证书路径', '微信支付沙箱环境证书文件路径', 'text', 6, 1, NOW(), NOW()),
('payment.wechat.production.appid', '', '微信生产AppID', '微信支付生产环境AppID（商户申请完成后填写）', 'text', 7, 1, NOW(), NOW()),
('payment.wechat.production.mchid', '', '微信生产商户号', '微信支付生产环境商户号（商户申请完成后填写）', 'text', 8, 1, NOW(), NOW()),
('payment.wechat.production.key', '', '微信生产API密钥', '微信支付生产环境API密钥（敏感信息，请妥善保管）', 'textarea', 9, 1, NOW(), NOW()),
('payment.wechat.production.cert_path', '', '微信生产证书路径', '微信支付生产环境证书文件路径', 'text', 10, 1, NOW(), NOW()),
('payment.wechat.notify_url', 'https://your-domain.com/api/buyer/payment/wechat/notify', '微信支付回调地址', '微信支付回调通知地址', 'text', 11, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_name = VALUES(config_name),
    config_desc = VALUES(config_desc),
    config_type = VALUES(config_type),
    sort_order = VALUES(sort_order),
    update_time = NOW();

-- 支付宝配置初始化
INSERT INTO system_config (config_key, config_value, config_name, config_desc, config_type, sort_order, status, create_time, update_time) VALUES
('payment.alipay.enabled', '1', '支付宝开关', '是否启用支付宝：1-启用，0-禁用', 'text', 20, 1, NOW(), NOW()),
('payment.alipay.env', 'sandbox', '支付宝环境', '支付环境：sandbox-沙箱，production-生产', 'text', 21, 1, NOW(), NOW()),
('payment.alipay.sandbox.appid', '', '支付宝沙箱AppID', '支付宝沙箱环境AppID', 'text', 22, 1, NOW(), NOW()),
('payment.alipay.sandbox.private_key', '', '支付宝沙箱应用私钥', '支付宝沙箱环境应用私钥（敏感信息，请妥善保管）', 'textarea', 23, 1, NOW(), NOW()),
('payment.alipay.sandbox.public_key', '', '支付宝沙箱公钥', '支付宝沙箱环境支付宝公钥', 'textarea', 24, 1, NOW(), NOW()),
('payment.alipay.production.appid', '', '支付宝生产AppID', '支付宝生产环境AppID（商户申请完成后填写）', 'text', 25, 1, NOW(), NOW()),
('payment.alipay.production.private_key', '', '支付宝生产应用私钥', '支付宝生产环境应用私钥（敏感信息，请妥善保管）', 'textarea', 26, 1, NOW(), NOW()),
('payment.alipay.production.public_key', '', '支付宝生产公钥', '支付宝生产环境支付宝公钥', 'textarea', 27, 1, NOW(), NOW()),
('payment.alipay.notify_url', 'https://your-domain.com/api/buyer/payment/alipay/notify', '支付宝回调地址', '支付宝回调通知地址', 'text', 28, 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
    config_name = VALUES(config_name),
    config_desc = VALUES(config_desc),
    config_type = VALUES(config_type),
    sort_order = VALUES(sort_order),
    update_time = NOW();

