-- ============================================
-- 更新脚本: update-20251227-optimize-shipping-rule-region.sql
-- 更新日期: 2025-12-27
-- 更新说明: 优化运费规则表结构，支持多地区存储（JSON格式）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 先删除旧的region_code索引（TEXT类型不支持普通索引，必须先删除索引才能修改字段类型）
-- 如果索引不存在，会报错，但可以忽略继续执行

-- 修改运费规则表，将地区编码和名称改为TEXT类型，支持JSON数组存储多个地区
ALTER TABLE `shipping_rule`
MODIFY COLUMN `region_code` TEXT COMMENT '地区编码（JSON数组，支持多个地区，格式：[{"provinceCode":"110000","cityCode":"110100","districtCode":"110101"},...]，为空表示默认规则）',
MODIFY COLUMN `region_name` TEXT COMMENT '地区名称（JSON数组，支持多个地区，格式：[{"provinceName":"北京市","cityName":"北京市","districtName":"东城区"},...]，为空表示默认规则）';

