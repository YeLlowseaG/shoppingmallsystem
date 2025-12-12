-- 添加价格和库存相关字段到product表
USE chengren_shopping_mall;

ALTER TABLE `product` 
ADD COLUMN `sale_price` decimal(10,2) DEFAULT NULL COMMENT '销售价格',
ADD COLUMN `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场价格',
ADD COLUMN `cost_price` decimal(10,2) DEFAULT NULL COMMENT '成本价格',
ADD COLUMN `warning_stock` int DEFAULT NULL COMMENT '警戒库存',
ADD COLUMN `weight` decimal(10,2) DEFAULT NULL COMMENT '商品重量(g)';

-- 验证表结构
DESCRIBE product;