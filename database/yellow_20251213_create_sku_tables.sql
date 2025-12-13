-- SKU规格系统数据库表设计
-- 创建时间：2025-12-13
-- 说明：实现标准电商SKU规格管理系统

USE chengren_shopping_mall;

-- 1. 商品规格属性表 (存储规格名称，如：颜色、尺寸、容量)
CREATE TABLE `product_spec_key` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸、容量）',
  `sort_order` int DEFAULT 0 COMMENT '排序权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格属性表';

-- 2. 商品规格值表 (存储具体规格值，如：红色、L码、500ml)
CREATE TABLE `product_spec_value` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spec_key_id` bigint NOT NULL COMMENT '规格属性ID',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值（如：红色、L码、500ml）',
  `spec_image` varchar(500) DEFAULT NULL COMMENT '规格图片URL（可选）',
  `sort_order` int DEFAULT 0 COMMENT '排序权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spec_key_id` (`spec_key_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格值表';

-- 3. 商品SKU表 (存储具体的商品规格组合)
CREATE TABLE `product_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(100) NOT NULL COMMENT 'SKU编码（唯一）',
  `spec_combination` json NOT NULL COMMENT '规格组合JSON（如：{"颜色":"红色","尺寸":"L"}）',
  `price` decimal(10,2) NOT NULL COMMENT 'SKU价格',
  `stock` int DEFAULT 0 COMMENT 'SKU库存',
  `warning_stock` int DEFAULT 0 COMMENT 'SKU警戒库存',
  `sales_count` int DEFAULT 0 COMMENT 'SKU销量',
  `weight` decimal(10,2) DEFAULT NULL COMMENT 'SKU重量(g)',
  `sku_image` varchar(500) DEFAULT NULL COMMENT 'SKU主图（可选）',
  `sku_images` json DEFAULT NULL COMMENT 'SKU图片列表（可选）',
  `status` tinyint DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_price` (`price`),
  KEY `idx_stock` (`stock`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- 添加外键约束
ALTER TABLE `product_spec_key` 
ADD CONSTRAINT `fk_spec_key_product` 
FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE;

ALTER TABLE `product_spec_value` 
ADD CONSTRAINT `fk_spec_value_key` 
FOREIGN KEY (`spec_key_id`) REFERENCES `product_spec_key` (`id`) ON DELETE CASCADE;

ALTER TABLE `product_sku` 
ADD CONSTRAINT `fk_sku_product` 
FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE;

-- 插入示例数据（用于测试）
-- 假设有一个商品ID为1的商品需要添加规格

-- 示例：为商品ID=1添加"包装规格"这个规格属性
INSERT INTO `product_spec_key` (`product_id`, `spec_name`, `sort_order`) 
VALUES (1, '包装规格', 1);

-- 获取刚插入的规格属性ID (这里假设是1)
SET @spec_key_id = LAST_INSERT_ID();

-- 为"包装规格"添加具体的规格值
INSERT INTO `product_spec_value` (`spec_key_id`, `spec_value`, `sort_order`) 
VALUES 
(@spec_key_id, '2只装', 1),
(@spec_key_id, '12只装', 2),
(@spec_key_id, '24只装', 3),
(@spec_key_id, '48只装', 4);

-- 为商品ID=1创建对应的SKU
INSERT INTO `product_sku` (`product_id`, `sku_code`, `spec_combination`, `price`, `stock`, `warning_stock`) 
VALUES 
(1, 'SKU001-2PCS', '{"包装规格": "2只装"}', 19.90, 100, 10),
(1, 'SKU001-12PCS', '{"包装规格": "12只装"}', 89.90, 50, 5),
(1, 'SKU001-24PCS', '{"包装规格": "24只装"}', 159.90, 30, 3),
(1, 'SKU001-48PCS', '{"包装规格": "48只装"}', 299.90, 20, 2);

-- 验证数据
SELECT '=== 规格属性表 ===' as info;
SELECT * FROM product_spec_key WHERE product_id = 1;

SELECT '=== 规格值表 ===' as info;
SELECT psv.*, psk.spec_name 
FROM product_spec_value psv 
JOIN product_spec_key psk ON psv.spec_key_id = psk.id 
WHERE psk.product_id = 1;

SELECT '=== SKU表 ===' as info;
SELECT * FROM product_sku WHERE product_id = 1;