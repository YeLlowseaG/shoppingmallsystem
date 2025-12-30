-- ============================================
-- 更新脚本: update-20251230-add-scheduled-task-table-and-init-data.sql
-- 更新日期: 2025-12-30
-- 更新说明: 创建定时任务表并初始化定时任务数据
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 创建定时任务表
CREATE TABLE IF NOT EXISTS `scheduled_task` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_group` varchar(50) NOT NULL DEFAULT 'DEFAULT' COMMENT '任务组',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'Cron表达式（固定频率任务可为空）',
  `bean_name` varchar(100) NOT NULL COMMENT 'Bean名称（Spring Bean名称）',
  `method_name` varchar(100) NOT NULL COMMENT '方法名称',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-已停止，1-运行中）',
  `description` varchar(500) DEFAULT NULL COMMENT '任务描述',
  `last_execute_time` datetime DEFAULT NULL COMMENT '上次执行时间',
  `next_execute_time` datetime DEFAULT NULL COMMENT '下次执行时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_name_group` (`task_name`, `task_group`),
  KEY `idx_status` (`status`),
  KEY `idx_bean_name` (`bean_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务表';

-- 插入初始化的定时任务数据
-- 1. 订单自动取消超时订单任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('订单自动取消超时订单', '订单管理', NULL, 'orderScheduledServiceImpl', 'cancelTimeoutOrders', 1, '每分钟执行一次，自动取消超过指定时间未支付的待付款订单，并恢复库存')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- 2. 预存款自动取消超时记录任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('预存款自动取消超时记录', '预存款管理', NULL, 'depositPayingScheduledService', 'cancelTimeoutDepositRecords', 1, '每小时执行一次，自动将超过指定时间仍处于支付中状态的预存款记录更新为已超时')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- 3. 支付记录自动关闭超时记录任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('支付记录自动关闭超时记录', '支付管理', NULL, 'paymentRecordScheduledServiceImpl', 'cancelTimeoutPaymentRecords', 1, '每小时执行一次，自动将超过指定时间仍处于支付中状态的支付记录更新为已关闭')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- 4. 订单补单查询任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('订单补单查询', '支付管理', NULL, 'paymentSyncScheduledService', 'syncPaymentStatus', 1, '每5分钟执行一次，查询支付中状态的支付记录，如果支付宝已支付则自动补单并更新订单状态')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- 5. 聚水潭物流拉取任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('聚水潭物流拉取', 'ERP同步', '0 */30 * * * ?', 'jushuitanSyncTask', 'pullLogisticsTask', 1, '每30分钟执行一次，拉取聚水潭ERP中待发货订单的物流信息（需配置启用）')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- 6. 聚水潭全量同步任务
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('聚水潭全量同步', 'ERP同步', '0 0 3 * * ?', 'jushuitanSyncTask', 'fullSyncTask', 1, '每天凌晨3点执行一次，执行聚水潭ERP全量同步任务（需配置启用）')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`);

-- ============================================
-- 说明：
-- 1. scheduled_task 表：存储系统定时任务配置信息
-- 2. 索引说明：
--    - uk_task_name_group: 任务名称和任务组的唯一索引，确保同一组内任务名称唯一
--    - idx_status: 按状态查询
--    - idx_bean_name: 按Bean名称查询
-- 3. 初始化任务说明：
--    - 订单自动取消超时订单：每分钟执行（fixedRate = 60000）
--    - 预存款自动取消超时记录：每小时执行（fixedRate = 3600000）
--    - 支付记录自动关闭超时记录：每小时执行（fixedRate = 3600000）
--    - 订单补单查询：每5分钟执行（fixedRate = 300000）
--    - 聚水潭物流拉取：每30分钟执行（cron = "0 */30 * * * ?"）
--    - 聚水潭全量同步：每天凌晨3点执行（cron = "0 0 3 * * ?"）
-- ============================================

