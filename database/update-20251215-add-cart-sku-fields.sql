-- 更新购物车表，添加sku_id和spec_combination字段
-- 执行时间：2025-12-15
-- 解决问题：Unknown column 'sku_id' in 'field list'

USE `chengren_shopping_mall`;

-- 添加sku_id字段（SKU ID，可为空）
ALTER TABLE `cart` ADD COLUMN `sku_id` bigint NULL COMMENT 'SKU ID（可为空，表示无规格或未选择）' AFTER `product_id`;

-- 添加spec_combination字段（规格组合，JSON字符串）
ALTER TABLE `cart` ADD COLUMN `spec_combination` varchar(500) NULL COMMENT '规格组合（JSON字符串，如 {"颜色":"红色","尺寸":"L"}）' AFTER `sku_id`;

-- 更新唯一键，考虑sku_id字段
ALTER TABLE `cart` DROP KEY `uk_user_product`;
ALTER TABLE `cart` ADD UNIQUE KEY `uk_user_product_sku` (`user_id`,`product_id`,`sku_id`) COMMENT '用户、商品、SKU的唯一组合';