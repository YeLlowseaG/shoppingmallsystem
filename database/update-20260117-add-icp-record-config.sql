-- ============================================
-- 更新脚本: update-20260117-add-icp-record-config.sql
-- 更新日期: 2026-01-17
-- 更新说明: 添加备案号配置项（3个）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 添加ICP备案号配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `category`, `sort_order`, `status`)
VALUES (
    'site.icp.number',
    '粤ICP备11098444号',
    'ICP备案号',
    '网站ICP备案号，显示在首页底部',
    'text',
    'site',
    9,
    1
)
ON DUPLICATE KEY UPDATE 
    `config_name` = VALUES(`config_name`),
    `config_value` = VALUES(`config_value`),
    `config_desc` = VALUES(`config_desc`),
    `update_time` = CURRENT_TIMESTAMP;

-- 添加医疗器械网络备案号配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `category`, `sort_order`, `status`)
VALUES (
    'site.icp.device.network',
    '粤深械网备202005070014',
    '医疗器械网络备案号',
    '医疗器械网络备案号，显示在首页底部',
    'text',
    'site',
    10,
    1
)
ON DUPLICATE KEY UPDATE 
    `config_name` = VALUES(`config_name`),
    `config_value` = VALUES(`config_value`),
    `config_desc` = VALUES(`config_desc`),
    `update_time` = CURRENT_TIMESTAMP;

-- 添加医疗器械经营备案号配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `category`, `sort_order`, `status`)
VALUES (
    'site.icp.device.management',
    '粤深食药监械经营备20151274号',
    '医疗器械经营备案号',
    '医疗器械经营备案号，显示在首页底部',
    'text',
    'site',
    11,
    1
)
ON DUPLICATE KEY UPDATE 
    `config_name` = VALUES(`config_name`),
    `config_value` = VALUES(`config_value`),
    `config_desc` = VALUES(`config_desc`),
    `update_time` = CURRENT_TIMESTAMP;

-- ============================================
-- 说明：
-- 1. site.icp.number - ICP备案号
-- 2. site.icp.device.network - 医疗器械网络备案号
-- 3. site.icp.device.management - 医疗器械经营备案号
-- 4. 如果配置值为空，前端将不显示该项
-- ============================================

