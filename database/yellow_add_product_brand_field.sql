-- 添加品牌ID字段到product表
USE chengren_shopping_mall;

-- 添加brand_id字段
ALTER TABLE `product` 
ADD COLUMN `brand_id` bigint DEFAULT NULL COMMENT '品牌ID（关联website_brand表）';

-- 添加外键约束（可选，建议生产环境使用）
-- ALTER TABLE `product` 
-- ADD CONSTRAINT `fk_product_brand` 
-- FOREIGN KEY (`brand_id`) REFERENCES `website_brand`(`id`) ON DELETE SET NULL;

-- 验证表结构
DESCRIBE `product`;

-- 查看现有商品数据
SELECT id, product_name, brand_id FROM `product` WHERE `deleted` = 0 LIMIT 5;