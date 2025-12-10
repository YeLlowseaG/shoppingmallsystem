-- ============================================
-- 更新脚本: update-20251210-add-pre-deposit-detail-fields.sql
-- 更新日期: 2025-12-10
-- 更新说明: 扩展预存款明细表，增加交易记录所需字段
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 扩展 pre_deposit_detail 表，增加交易记录所需字段
-- 注意：MySQL 的 ALTER TABLE ADD COLUMN 不支持 IF NOT EXISTS
-- 如果字段已存在，执行此脚本会报错，需要手动检查或使用存储过程检查字段是否存在
ALTER TABLE `pre_deposit_detail`
ADD COLUMN `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '存入金额' AFTER `amount`,
ADD COLUMN `expense_amount` decimal(10,2) DEFAULT '0.00' COMMENT '支出金额' AFTER `deposit_amount`,
ADD COLUMN `frozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结金额' AFTER `expense_amount`,
ADD COLUMN `unfrozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '解冻金额' AFTER `frozen_amount`,
ADD COLUMN `current_balance` decimal(10,2) DEFAULT '0.00' COMMENT '当前余额' AFTER `unfrozen_amount`,
ADD COLUMN `available_balance` decimal(10,2) DEFAULT '0.00' COMMENT '可用余额' AFTER `current_balance`,
ADD COLUMN `event` varchar(100) DEFAULT NULL COMMENT '事件描述（如：预存款支付、在线充值、预存款退款、代充值）' AFTER `available_balance`,
ADD COLUMN `remark` varchar(500) DEFAULT NULL COMMENT '备注' AFTER `event`,
ADD COLUMN `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（如果是订单相关操作）' AFTER `remark`,
ADD COLUMN `order_no` varchar(50) DEFAULT NULL COMMENT '关联订单号' AFTER `order_id`;

-- 添加外部交易号字段
ALTER TABLE `pre_deposit_detail`
ADD COLUMN `external_trade_no` varchar(100) DEFAULT NULL COMMENT '外部交易号（微信/支付宝返回的交易号）' AFTER `order_no`;

-- 添加内部订单号字段（用于精确查找充值记录）
ALTER TABLE `pre_deposit_detail`
ADD COLUMN `internal_order_no` varchar(100) DEFAULT NULL COMMENT '内部订单号（用于充值记录查找）' AFTER `external_trade_no`;

-- 添加索引
-- 注意：如果索引已存在，执行此脚本会报错，需要手动检查或使用存储过程检查索引是否存在
ALTER TABLE `pre_deposit_detail`
ADD KEY `idx_order_id` (`order_id`),
ADD KEY `idx_order_no` (`order_no`),
ADD KEY `idx_external_trade_no` (`external_trade_no`),
ADD KEY `idx_internal_order_no` (`internal_order_no`),
ADD KEY `idx_event` (`event`),
ADD KEY `idx_user_event_time` (`user_id`, `event`, `create_time`),
ADD KEY `idx_status_create_time` (`status`, `create_time`);

