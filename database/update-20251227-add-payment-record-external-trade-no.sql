-- ============================================
-- 更新脚本: update-20251227-add-payment-record-external-trade-no.sql
-- 更新日期: 2025-12-27
-- 更新说明: 为支付记录表添加外部交易号字段（用于保存支付宝/微信返回的交易号）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 为支付记录表添加外部交易号字段
-- 用于保存支付宝返回的 trade_no 或微信返回的 transaction_id
ALTER TABLE `payment_record`
ADD COLUMN `external_trade_no` varchar(100) DEFAULT NULL COMMENT '外部交易号（支付宝返回的trade_no或微信返回的transaction_id）' AFTER `payment_no`;

-- 添加索引，方便查询
ALTER TABLE `payment_record`
ADD KEY `idx_external_trade_no` (`external_trade_no`);

