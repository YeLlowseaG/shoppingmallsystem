-- 为现有商品设置价格和库存字段的默认值
USE chengren_shopping_mall;

-- 更新现有商品的默认值
UPDATE `product` 
SET 
    -- 销售价格设置为基础批发价（如果基础价格存在）
    `sale_price` = CASE WHEN `sale_price` IS NULL THEN `base_price` ELSE `sale_price` END,
    
    -- 市场价格设置为基础价格的1.2倍（常见电商做法）
    `market_price` = CASE WHEN `market_price` IS NULL THEN ROUND(`base_price` * 1.2, 2) ELSE `market_price` END,
    
    -- 成本价格设置为基础价格的0.7倍
    `cost_price` = CASE WHEN `cost_price` IS NULL THEN ROUND(`base_price` * 0.7, 2) ELSE `cost_price` END,
    
    -- 警戒库存设置为当前库存的20%，最少10个
    `warning_stock` = CASE WHEN `warning_stock` IS NULL THEN GREATEST(ROUND(`stock` * 0.2), 10) ELSE `warning_stock` END,
    
    -- 商品重量设置为500g（默认值）
    `weight` = CASE WHEN `weight` IS NULL THEN 500.00 ELSE `weight` END

WHERE `id` IS NOT NULL;

-- 验证更新结果
SELECT 
    id,
    product_name,
    base_price,
    sale_price,
    market_price,
    cost_price,
    stock,
    warning_stock,
    weight
FROM `product` 
WHERE `deleted` = 0 
ORDER BY `id` 
LIMIT 10;