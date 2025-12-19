-- ============================================
-- 更新脚本: update-20251219-add-member-price-fields.sql
-- 更新日期: 2025-12-19
-- 更新说明: 为商品和SKU添加会员价功能
--           1. product表添加 member_price, enable_member_price 字段（支持无规格商品）
--           2. product_sku表添加 member_price, enable_member_price,
--              suggested_retail_price, market_retail_price 字段（支持有规格商品）
-- 作者: ShoppingMall Team
--
-- 业务逻辑：
--   - enable_member_price = 1 时，使用 member_price 作为售价
--   - enable_member_price = 0 时，使用 base_price/price 作为售价
-- ============================================

USE chengren_shopping_mall;

-- ============================================
-- 1. 为 product 表添加会员价字段（无规格商品使用）
-- ============================================
-- 添加会员价字段
ALTER TABLE `product`
ADD COLUMN `member_price` decimal(10,2) DEFAULT NULL COMMENT '会员价（启用时作为售价）' AFTER `market_retail_price`,
ADD COLUMN `enable_member_price` tinyint(1) DEFAULT 0 COMMENT '是否启用会员价：0-否，1-是' AFTER `member_price`;

-- ============================================
-- 2. 为 product_sku 表添加价格相关字段（有规格商品使用）
-- ============================================
-- 添加建议零售价
ALTER TABLE `product_sku`
ADD COLUMN `suggested_retail_price` decimal(10,2) DEFAULT NULL COMMENT '建议零售价' AFTER `price`;

-- 添加市场零售价
ALTER TABLE `product_sku`
ADD COLUMN `market_retail_price` decimal(10,2) DEFAULT NULL COMMENT '市场零售价' AFTER `suggested_retail_price`;

-- 添加会员价
ALTER TABLE `product_sku`
ADD COLUMN `member_price` decimal(10,2) DEFAULT NULL COMMENT '会员价（启用时作为售价）' AFTER `market_retail_price`;

-- 添加是否启用会员价开关
ALTER TABLE `product_sku`
ADD COLUMN `enable_member_price` tinyint(1) DEFAULT 0 COMMENT '是否启用会员价：0-否，1-是' AFTER `member_price`;

-- ============================================
-- 3. 验证修改结果
-- ============================================
-- 查看 product 表结构
DESCRIBE `product`;

-- 查看 product_sku 表结构
DESCRIBE `product_sku`;

-- ============================================
-- 4. 数据完整性验证
-- ============================================
-- 验证 product 表新字段
SELECT
    id,
    product_name,
    base_price,
    suggested_retail_price,
    market_retail_price,
    member_price,
    enable_member_price,
    enable_spec
FROM `product`
WHERE `deleted` = 0
LIMIT 5;

-- 验证 product_sku 表新字段
SELECT
    id,
    product_id,
    sku_code,
    price,
    suggested_retail_price,
    market_retail_price,
    member_price,
    enable_member_price
FROM `product_sku`
LIMIT 5;

-- ============================================
-- 更新完成
-- ============================================
-- 字段添加完成，请确保：
-- 1. 后端代码已更新（Product.java, ProductSku.java 及相关DTO、VO）
-- 2. 前端代码已更新（商品表单、SKU列表组件）
-- 3. 所有相关功能已测试验证
--
-- 价格计算逻辑：
-- - 无规格商品(enable_spec=0):
--   若 enable_member_price=1 且 member_price 有值，售价=member_price
--   否则售价=base_price
-- - 有规格商品(enable_spec=1):
--   若 SKU 的 enable_member_price=1 且 member_price 有值，售价=member_price
--   否则售价=price（SKU基础价）
-- ============================================
