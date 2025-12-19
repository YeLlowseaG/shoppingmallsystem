-- ============================================
-- 更新脚本: update-20251219-rename-product-price-fields.sql
-- 更新日期: 2025-12-19
-- 更新说明: 重命名商品价格字段，使字段名称与业务含义一致
--           将 market_price 改为 suggested_retail_price（建议零售价）
--           将 cost_price 改为 market_retail_price（市场零售价）
-- 作者: ShoppingMall Team
-- 
-- 注意：此脚本为字段重命名操作，已同步更新所有相关代码
--       - 后端Java代码（Entity、DTO、VO）
--       - 前端API类型定义和页面组件
--       - 数据库表结构定义文件
-- ============================================

USE chengren_shopping_mall;

-- ============================================
-- 1. 备份检查（可选，建议在生产环境执行前先备份）
-- ============================================
-- CREATE TABLE product_backup_20251219 AS SELECT * FROM product;

-- ============================================
-- 2. 重命名字段名称和更新注释
-- ============================================
-- 说明：使用 CHANGE COLUMN 同时修改字段名称、类型和注释
--       此操作会保留原有数据，只是更改字段名称
ALTER TABLE `product` 
CHANGE COLUMN `market_price` `suggested_retail_price` decimal(10,2) DEFAULT NULL COMMENT '建议零售价',
CHANGE COLUMN `cost_price` `market_retail_price` decimal(10,2) DEFAULT NULL COMMENT '市场零售价';

-- ============================================
-- 3. 验证修改结果
-- ============================================
-- 查看表结构，确认字段已重命名
DESCRIBE `product`;

-- ============================================
-- 4. 数据完整性验证
-- ============================================
-- 查询验证（检查字段是否存在且数据完整）
SELECT 
    id,
    product_name,
    base_price,
    suggested_retail_price,
    market_retail_price,
    stock
FROM `product` 
WHERE `deleted` = 0 
LIMIT 5;

-- ============================================
-- 更新完成
-- ============================================
-- 字段重命名完成，请确保：
-- 1. 后端代码已更新（Product.java, ProductDTO.java, ProductVO.java）
-- 2. 前端代码已更新（API类型定义和页面组件）
-- 3. 所有相关功能已测试验证
-- ============================================
