-- 订单同步日志添加环境类型字段
-- 用于区分测试环境和生产环境的同步记录
-- 日期：2025-12-31

-- 添加环境类型字段
ALTER TABLE `order_sync_log`
ADD COLUMN `env_type` VARCHAR(20) DEFAULT NULL COMMENT '环境类型：test=测试环境，production=生产环境' AFTER `order_no`;

-- 创建索引，方便按环境类型查询
CREATE INDEX `idx_env_type` ON `order_sync_log` (`env_type`);

-- 查看表结构
DESC `order_sync_log`;
