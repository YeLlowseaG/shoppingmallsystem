-- 添加聚水潭配置表的自动同步商品字段
-- 执行时间: 2025-01-14
-- 说明: 为jushuitan_config表添加auto_sync_product字段，用于控制是否自动同步商品

-- 添加auto_sync_product字段，默认值为0（关闭）
ALTER TABLE `jushuitan_config`
ADD COLUMN `auto_sync_product` int(11) NOT NULL DEFAULT 0 COMMENT '是否自动同步商品（0-否，1-是）' AFTER `auto_push_order`;

-- 初始化现有记录的默认值
UPDATE `jushuitan_config` SET `auto_sync_product` = 0 WHERE `auto_sync_product` IS NULL;

-- 添加注释
ALTER TABLE `jushuitan_config` COMMENT '聚水潭ERP配置表，支持订单推送、物流拉取和商品同步';
