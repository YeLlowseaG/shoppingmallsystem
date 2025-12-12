-- 数据迁移：现有商品数据适配SKU结构
-- 创建时间：2025-12-13
-- 说明：为现有商品创建默认SKU，保持商品数据的兼容性

USE chengren_shopping_mall;

-- 开始事务
START TRANSACTION;

-- 1. 为所有现有商品添加默认规格属性"包装规格"
INSERT INTO `product_spec_key` (`product_id`, `spec_name`, `sort_order`, `create_time`, `update_time`)
SELECT 
    id as product_id,
    '包装规格' as spec_name,
    1 as sort_order,
    CURRENT_TIMESTAMP as create_time,
    CURRENT_TIMESTAMP as update_time
FROM `product`
WHERE id NOT IN (
    SELECT DISTINCT product_id FROM `product_spec_key`
);

-- 2. 为每个新添加的规格属性添加默认规格值"标准"
INSERT INTO `product_spec_value` (`spec_key_id`, `spec_value`, `sort_order`, `create_time`, `update_time`)
SELECT 
    psk.id as spec_key_id,
    '标准' as spec_value,
    1 as sort_order,
    CURRENT_TIMESTAMP as create_time,
    CURRENT_TIMESTAMP as update_time
FROM `product_spec_key` psk
WHERE psk.spec_name = '包装规格'
AND psk.id NOT IN (
    SELECT DISTINCT spec_key_id FROM `product_spec_value`
);

-- 3. 为每个现有商品创建默认SKU
INSERT INTO `product_sku` (`product_id`, `sku_code`, `spec_combination`, `price`, `stock`, `warning_stock`, `sales_count`, `weight`, `status`, `create_time`, `update_time`)
SELECT 
    p.id as product_id,
    CONCAT(p.product_code, '-STD') as sku_code,
    '{"包装规格": "标准"}' as spec_combination,
    p.base_price as price,
    p.stock as stock,
    IFNULL(p.warning_stock, 0) as warning_stock,
    IFNULL(p.sales_count, 0) as sales_count,
    IFNULL(p.weight, 0) as weight,
    1 as status,
    CURRENT_TIMESTAMP as create_time,
    CURRENT_TIMESTAMP as update_time
FROM `product` p
WHERE p.id NOT IN (
    SELECT DISTINCT product_id FROM `product_sku`
);

-- 4. 更新商品表，添加enable_spec字段（如果不存在）
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = 'chengren_shopping_mall' 
     AND TABLE_NAME = 'product' 
     AND COLUMN_NAME = 'enable_spec') > 0,
    'SELECT "Column enable_spec already exists"',
    'ALTER TABLE `product` ADD COLUMN `enable_spec` TINYINT(1) DEFAULT 0 COMMENT "是否启用规格：0-否，1-是"'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 5. 为已有SKU的商品设置enable_spec为1
UPDATE `product` 
SET enable_spec = 1 
WHERE id IN (
    SELECT DISTINCT product_id FROM `product_sku`
);

-- 验证数据迁移结果
SELECT '=== 数据迁移验证 ===' as info;

-- 检查规格属性数量
SELECT 
    COUNT(*) as total_spec_keys,
    (SELECT COUNT(DISTINCT id) FROM `product`) as total_products
FROM `product_spec_key`;

-- 检查规格值数量
SELECT 
    COUNT(*) as total_spec_values
FROM `product_spec_value`;

-- 检查SKU数量
SELECT 
    COUNT(*) as total_skus,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_skus,
    COUNT(CASE WHEN stock > 0 THEN 1 END) as in_stock_skus
FROM `product_sku`;

-- 检查商品表的enable_spec字段
SELECT 
    COUNT(CASE WHEN enable_spec = 1 THEN 1 END) as enabled_spec_products,
    COUNT(CASE WHEN enable_spec = 0 THEN 1 END) as disabled_spec_products
FROM `product`;

-- 显示前5个商品的SKU信息作为样例
SELECT '=== SKU示例数据 ===' as info;
SELECT 
    p.id as product_id,
    p.product_name,
    p.product_code,
    ps.sku_code,
    ps.spec_combination,
    ps.price,
    ps.stock,
    ps.status
FROM `product` p
JOIN `product_sku` ps ON p.id = ps.product_id
ORDER BY p.id
LIMIT 5;

-- 提交事务
COMMIT;

-- 输出完成信息
SELECT 'SKU数据迁移完成！所有现有商品已适配SKU结构，使用默认"包装规格:标准"配置。' as result;