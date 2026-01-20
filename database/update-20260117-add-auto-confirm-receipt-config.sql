-- ============================================
-- 更新脚本: update-20260117-add-auto-confirm-receipt-config.sql
-- 更新日期: 2026-01-17
-- 更新说明: 添加订单自动确认收货天数配置和定时任务记录
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 添加订单自动确认收货天数配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_name`, `config_desc`, `config_type`, `category`, `sort_order`, `status`)
VALUES (
    'order.auto-confirm-receipt-days',
    '15',
    '订单自动确认收货天数',
    '已发货订单超过指定天数后自动确认收货，默认15天',
    'number',
    'order',
    100,
    1
)
ON DUPLICATE KEY UPDATE 
    `config_name` = VALUES(`config_name`),
    `config_value` = VALUES(`config_value`),
    `config_desc` = VALUES(`config_desc`),
    `update_time` = CURRENT_TIMESTAMP;

-- 插入自动确认收货定时任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES (
    '订单自动确认收货',
    '订单管理',
    '0 30 0 * * ?',
    'orderScheduledServiceImpl',
    'autoConfirmReceipt',
    1,
    '每天凌晨12点30分执行一次，对已发货超过指定天数的订单自动确认收货（默认15天，可在系统配置中修改）'
)
ON DUPLICATE KEY UPDATE 
    `cron_expression` = VALUES(`cron_expression`),
    `bean_name` = VALUES(`bean_name`),
    `method_name` = VALUES(`method_name`),
    `description` = VALUES(`description`),
    `update_time` = CURRENT_TIMESTAMP;

-- ============================================
-- 说明：
-- 1. config_key: order.auto-confirm-receipt-days - 配置键
-- 2. config_value: 15 - 默认值15天
-- 3. 定时任务会在每天凌晨12点30分执行，自动确认已发货超过配置天数的订单
-- 4. 可以通过管理后台修改此配置值，修改后下次执行定时任务时生效
-- 5. Cron表达式说明：0 30 0 * * ? 表示每天凌晨0点30分0秒执行
-- ============================================

