-- ============================================
-- 更新脚本: update-20251230-add-scheduled-task-execution-log.sql
-- 更新日期: 2025-12-30
-- 更新说明: 创建定时任务执行日志表，用于记录手动执行的定时任务日志（不记录自动执行的日志）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 创建定时任务执行日志表
CREATE TABLE IF NOT EXISTS `scheduled_task_execution_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `task_id` bigint NOT NULL COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_group` varchar(50) DEFAULT NULL COMMENT '任务组',
  `bean_name` varchar(100) NOT NULL COMMENT 'Bean名称',
  `method_name` varchar(100) NOT NULL COMMENT '方法名称',
  `execute_status` tinyint NOT NULL COMMENT '执行状态（0-失败，1-成功）',
  `start_time` datetime NOT NULL COMMENT '开始执行时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束执行时间',
  `duration` bigint DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `error_message` text COMMENT '错误信息',
  `execute_type` tinyint NOT NULL DEFAULT '2' COMMENT '执行类型（固定为2-手动执行，不记录自动执行的日志）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_task_id` (`task_id`),
  KEY `idx_task_name` (`task_name`),
  KEY `idx_execute_status` (`execute_status`),
  KEY `idx_start_time` (`start_time`),
  KEY `idx_execute_type` (`execute_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='定时任务执行日志表';

-- ============================================
-- 说明：
-- 1. scheduled_task_execution_log 表：仅记录手动执行的定时任务日志
-- 2. 注意：自动执行的定时任务（通过@Scheduled注解）不会记录日志，只有通过管理后台手动触发的执行才会记录
-- 3. execute_type 字段固定为 2（手动执行），不记录自动执行的日志
-- 4. 索引说明：
--    - idx_task_id: 按任务ID查询
--    - idx_task_name: 按任务名称查询
--    - idx_execute_status: 按执行状态查询
--    - idx_start_time: 按开始时间查询
--    - idx_execute_type: 按执行类型查询（目前固定为手动执行）
-- ============================================

