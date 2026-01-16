-- ============================================
-- 添加ERP内部订单号字段
-- 创建时间：2026-01-15
-- 说明：在order表中添加erp_internal_order_id字段，用于存储聚水潭返回的o_id
-- ============================================

-- 添加ERP内部订单号字段
ALTER TABLE `order`
ADD COLUMN `erp_internal_order_id` varchar(50) DEFAULT NULL COMMENT 'ERP内部订单号（聚水潭返回的o_id）' AFTER `erp_order_id`;

-- 添加索引（可选，如果需要根据内部订单号查询）
ALTER TABLE `order` ADD INDEX `idx_erp_internal_order_id` (`erp_internal_order_id`);

-- ============================================
-- 说明：
-- erp_internal_order_id: 存储聚水潭API返回的o_id字段值
-- ============================================

