-- ============================================
-- 更新脚本: update-20251227-add-pre-deposit-detail-callback-data.sql
-- 更新日期: 2025-12-27
-- 更新说明: 为预存款明细表添加回调数据字段（用于保存第三方支付返回的数据）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 为预存款明细表添加回调数据字段
-- 用于保存支付宝/微信支付回调返回的完整数据（JSON格式）
-- 与 payment_record 表的 callback_data 字段设计保持一致
ALTER TABLE `pre_deposit_detail`
ADD COLUMN `callback_data` json DEFAULT NULL COMMENT '回调数据（JSON格式，保存第三方支付返回的完整数据）' AFTER `internal_order_no`;

