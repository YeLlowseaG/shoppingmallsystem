-- ============================================
-- 更新脚本: update-20251230-add-payment-api-log.sql
-- 更新日期: 2025-12-30
-- 更新说明: 创建支付接口日志表，用于记录第三方支付接口的交互日志
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 创建支付接口日志表
CREATE TABLE IF NOT EXISTS `payment_api_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `payment_method` varchar(20) NOT NULL COMMENT '支付方式（ALIPAY-支付宝，WECHAT-微信）',
  `api_type` varchar(50) NOT NULL COMMENT '接口类型（CREATE_PAYMENT-创建支付，REFUND-退款，QUERY_ORDER-查询订单，CALLBACK-回调通知）',
  `business_type` varchar(20) NOT NULL COMMENT '业务类型（ORDER-订单支付，DEPOSIT-预存款充值）',
  `order_no` varchar(100) DEFAULT NULL COMMENT '订单号（订单号或内部订单号）',
  `payment_no` varchar(100) DEFAULT NULL COMMENT '支付流水号',
  `external_trade_no` varchar(100) DEFAULT NULL COMMENT '外部交易号',
  `api_url` varchar(500) DEFAULT NULL COMMENT '接口URL',
  `request_method` varchar(10) DEFAULT NULL COMMENT 'HTTP请求方法（GET/POST）',
  `request_data` text COMMENT '请求数据（JSON/XML格式）',
  `response_data` text COMMENT '响应数据（JSON/XML格式）',
  `http_status_code` int DEFAULT NULL COMMENT 'HTTP状态码',
  `api_status` tinyint NOT NULL COMMENT '接口调用状态（0-失败，1-成功，2-处理中）',
  `error_code` varchar(50) DEFAULT NULL COMMENT '错误代码',
  `error_message` text COMMENT '错误信息',
  `execution_time` int DEFAULT NULL COMMENT '执行耗时（毫秒）',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_payment_method` (`payment_method`),
  KEY `idx_api_type` (`api_type`),
  KEY `idx_business_type` (`business_type`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_payment_no` (`payment_no`),
  KEY `idx_external_trade_no` (`external_trade_no`),
  KEY `idx_api_status` (`api_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付接口日志表';

-- ============================================
-- 说明：
-- 1. payment_api_log 表：记录所有第三方支付接口的交互日志
-- 2. 索引说明：
--    - idx_payment_method: 按支付方式查询
--    - idx_api_type: 按接口类型查询
--    - idx_business_type: 按业务类型查询
--    - idx_order_no: 按订单号查询
--    - idx_payment_no: 按支付流水号查询
--    - idx_external_trade_no: 按外部交易号查询
--    - idx_api_status: 按接口调用状态查询
--    - idx_create_time: 按创建时间查询
-- ============================================

