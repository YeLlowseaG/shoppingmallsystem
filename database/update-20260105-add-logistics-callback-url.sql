-- 添加物流同步回调地址字段
-- 日期：2026-01-05
-- 说明：为jushuitan_config表添加测试环境和生产环境的物流同步回调地址字段

ALTER TABLE `jushuitan_config` 
ADD COLUMN `test_callback_url` VARCHAR(500) NULL COMMENT '测试环境物流同步回调地址' AFTER `test_shop_id`,
ADD COLUMN `callback_url` VARCHAR(500) NULL COMMENT '生产环境物流同步回调地址' AFTER `shop_id`;

