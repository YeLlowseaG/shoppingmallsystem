-- ============================================
-- 更新脚本: update-20251227-add-product-shipping-template.sql
-- 更新日期: 2025-12-27
-- 更新说明: 为商品表添加运费模板关联字段
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 为商品表添加运费模板字段
ALTER TABLE `product`
ADD COLUMN `shipping_template_id` BIGINT NULL COMMENT '运费模板ID（关联shipping_template表，为空表示包邮）' AFTER `brand_id`,
ADD INDEX `idx_shipping_template` (`shipping_template_id`);

-- 为订单商品表添加运费模板字段（用于记录下单时使用的运费模板）
ALTER TABLE `order_item`
ADD COLUMN `shipping_template_id` BIGINT NULL COMMENT '运费模板ID（记录下单时商品使用的运费模板）' AFTER `product_id`;

