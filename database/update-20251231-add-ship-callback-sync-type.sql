-- 更新订单同步日志表注释，添加发货回调同步类型
-- 日期：2025-12-31

-- 更新 sync_type 字段注释，添加 SHIP_CALLBACK 类型说明
ALTER TABLE `order_sync_log` 
MODIFY COLUMN `sync_type` varchar(20) NOT NULL COMMENT '同步类型（PUSH_ORDER-推送订单，PULL_LOGISTICS-拉取物流，QUERY_ORDER-查询订单，SHIP_CALLBACK-发货回调）';

-- 验证更新结果
DESC `order_sync_log`;

