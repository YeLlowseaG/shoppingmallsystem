-- ============================================
-- 更新脚本: update-20251227-add-payment-sync-config.sql
-- 更新日期: 2025-12-27
-- 更新说明: 添加支付结果查询补单相关配置
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 支付结果查询时间窗口（分钟），只查询最近N分钟内的支付记录，默认30分钟
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`, `create_time`, `update_time`) 
VALUES ('payment.sync-time-window-minutes', '30', '支付结果查询时间窗口（分钟），只查询最近N分钟内的支付记录', NOW(), NOW())
ON DUPLICATE KEY UPDATE `config_value` = '30', `update_time` = NOW();

