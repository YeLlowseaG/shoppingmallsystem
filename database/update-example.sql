-- ============================================
-- 更新脚本示例: update-example.sql
-- 更新日期: YYYY-MM-DD
-- 更新说明: 简要说明本次更新的内容
-- 作者: 姓名
-- ============================================
-- 
-- 本文件为数据库更新脚本的示例模板
-- 实际使用时请复制此文件并重命名为: update-YYYYMMDD-description.sql
-- 
-- 更新规则：
-- 1. 只增加，不删除（字段、表、索引等）
-- 2. 新增字段必须设置默认值或允许NULL
-- 3. 使用 IF NOT EXISTS 确保脚本可重复执行
-- 4. 添加详细的注释说明
-- ============================================

-- ============================================
-- 示例1: 添加新表
-- ============================================
CREATE TABLE IF NOT EXISTS `example_table` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `name` varchar(100) NOT NULL COMMENT '名称',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='示例表';

-- ============================================
-- 示例2: 添加新字段（允许NULL）
-- ============================================
ALTER TABLE `order` 
ADD COLUMN IF NOT EXISTS `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注说明' AFTER `order_remark`;

-- ============================================
-- 示例3: 添加新字段（设置默认值）
-- ============================================
ALTER TABLE `order` 
ADD COLUMN IF NOT EXISTS `priority` TINYINT DEFAULT '0' COMMENT '优先级（0-普通，1-高，2-紧急）' AFTER `order_status`;

-- ============================================
-- 示例4: 添加新索引
-- ============================================
CREATE INDEX IF NOT EXISTS `idx_order_priority_time` ON `order` (`priority`, `create_time`);

-- ============================================
-- 示例5: 添加唯一索引
-- ============================================
CREATE UNIQUE INDEX IF NOT EXISTS `uk_order_custom_no` ON `order` (`custom_order_no`);

-- ============================================
-- 示例6: 更新现有数据（为新字段设置默认值）
-- ============================================
-- UPDATE `order` SET `priority` = 0 WHERE `priority` IS NULL;

-- ============================================
-- 注意事项：
-- 1. 所有 ALTER TABLE 语句必须使用 IF NOT EXISTS（MySQL 8.0+）
--    对于 MySQL 5.7，需要先检查字段是否存在
-- 2. 新增字段必须设置默认值或允许NULL
-- 3. 使用 AFTER 指定字段位置，保持表结构清晰
-- 4. 添加详细的 COMMENT 说明字段用途
-- 5. 索引命名使用 idx_ 前缀
-- 6. 唯一索引命名使用 uk_ 前缀
-- ============================================

