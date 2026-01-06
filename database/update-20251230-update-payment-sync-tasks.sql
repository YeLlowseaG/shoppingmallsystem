-- ============================================
-- 更新脚本: update-20251230-update-payment-sync-tasks.sql
-- 更新日期: 2025-12-30
-- 更新说明: 更新订单补单查询任务描述，添加预存款充值支付状态同步任务
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 更新订单补单查询任务的描述（支持支付宝和微信）
UPDATE `scheduled_task` 
SET `description` = '每5分钟执行一次，查询支付中状态的支付记录（支持支付宝和微信），如果已支付则自动补单并更新订单状态'
WHERE `task_name` = '订单补单查询';

-- 添加预存款充值支付状态同步任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('预存款充值支付状态同步', '支付管理', NULL, 'depositPaymentSyncScheduledService', 'syncDepositPaymentStatus', 1, '每5分钟执行一次，查询支付中状态的预存款充值记录（支持支付宝和微信），如果已支付则自动更新充值状态和余额')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- ============================================
-- 说明：
-- 1. 订单补单查询任务：已更新描述，支持支付宝和微信支付
-- 2. 预存款充值支付状态同步任务：新增任务，支持支付宝和微信支付的预存款充值状态查询和同步
-- ============================================












