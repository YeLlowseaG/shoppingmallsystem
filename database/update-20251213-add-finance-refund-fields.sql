-- ============================================
-- 更新脚本: update-20251213-add-finance-refund-fields.sql
-- 更新日期: 2025-12-13
-- 更新说明: 为财务管理模块添加退款相关字段
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 为支付记录表增加退款相关字段
ALTER TABLE `payment_record`
ADD COLUMN `refunded_amount` decimal(10,2) DEFAULT '0.00' COMMENT '已退款金额' AFTER `amount`,
ADD COLUMN `refund_time` datetime DEFAULT NULL COMMENT '退款时间' AFTER `payment_time`,
ADD COLUMN `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因' AFTER `refund_time`,
ADD COLUMN `refund_operator_id` bigint DEFAULT NULL COMMENT '退款操作人ID（管理员）' AFTER `refund_reason`,
ADD COLUMN `refund_operator_name` varchar(50) DEFAULT NULL COMMENT '退款操作人姓名' AFTER `refund_operator_id`;

-- 添加索引
ALTER TABLE `payment_record`
ADD KEY `idx_refunded_amount` (`refunded_amount`);




