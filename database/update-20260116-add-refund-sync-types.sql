-- ============================================
-- 更新订单同步日志表，添加退款相关同步类型
-- 创建时间：2026-01-16
-- 说明：在order_sync_log表的sync_type字段注释中添加CANCEL_ORDER和UPDATE_ORDER_REFUND类型
-- ============================================

-- 更新 sync_type 字段注释，添加订单取消和订单更新同步类型
ALTER TABLE `order_sync_log` 
MODIFY COLUMN `sync_type` varchar(20) NOT NULL COMMENT '同步类型（PUSH_ORDER-推送订单，PULL_LOGISTICS-拉取物流，QUERY_ORDER-查询订单，SHIP_CALLBACK-发货回调，CANCEL_ORDER-订单取消，UPDATE_ORDER_REFUND-订单更新）';

-- 验证更新结果
DESC `order_sync_log`;









