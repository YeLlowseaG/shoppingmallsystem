-- ============================================
-- 聚水潭ERP对接功能数据库脚本
-- 创建时间：2025-12-24
-- 说明：添加聚水潭ERP对接所需的表和字段
-- ============================================

-- 1. 创建聚水潭配置表
CREATE TABLE IF NOT EXISTS `jushuitan_config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `app_key` varchar(100) NOT NULL COMMENT '聚水潭AppKey',
  `app_secret` varchar(200) NOT NULL COMMENT '聚水潭AppSecret',
  `api_url` varchar(200) DEFAULT 'https://open.jushuitan.com/api/open/query.aspx' COMMENT 'API地址',
  `shop_id` varchar(50) DEFAULT NULL COMMENT '店铺编号（多店铺时使用）',
  `partner_id` varchar(50) DEFAULT NULL COMMENT '合作伙伴ID',
  `enabled` tinyint(1) DEFAULT '1' COMMENT '是否启用（0-否，1-是）',
  `auto_push_order` tinyint(1) DEFAULT '1' COMMENT '是否自动推送订单（0-否，1-是）',
  `auto_pull_logistics` tinyint(1) DEFAULT '1' COMMENT '是否自动拉取物流（0-否，1-是）',
  `pull_interval` int(11) DEFAULT '5' COMMENT '拉取物流间隔（分钟）',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聚水潭配置表';

-- 2. 创建订单同步日志表
CREATE TABLE IF NOT EXISTS `order_sync_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint(20) NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `sync_type` varchar(20) NOT NULL COMMENT '同步类型（PUSH_ORDER-推送订单，PULL_LOGISTICS-拉取物流，QUERY_ORDER-查询订单）',
  `sync_status` tinyint(1) NOT NULL COMMENT '同步状态（0-失败，1-成功，2-处理中）',
  `error_code` varchar(50) DEFAULT NULL COMMENT '错误代码',
  `error_message` text COMMENT '错误信息',
  `request_data` text COMMENT '请求数据（JSON格式）',
  `response_data` text COMMENT '响应数据（JSON格式）',
  `retry_count` int(11) DEFAULT '0' COMMENT '重试次数',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_sync_type` (`sync_type`),
  KEY `idx_sync_status` (`sync_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单同步日志表';

-- 3. 修改订单表，添加ERP同步相关字段
ALTER TABLE `order`
ADD COLUMN `erp_sync_status` tinyint(1) DEFAULT '0' COMMENT 'ERP同步状态（0-未同步，1-已同步，2-同步失败）' AFTER `order_status`,
ADD COLUMN `erp_sync_time` datetime DEFAULT NULL COMMENT 'ERP同步时间' AFTER `erp_sync_status`,
ADD COLUMN `erp_order_id` varchar(50) DEFAULT NULL COMMENT 'ERP订单ID' AFTER `erp_sync_time`,
ADD COLUMN `seller_remark` varchar(500) DEFAULT NULL COMMENT '卖家备注' AFTER `order_remark`;

-- 4. 为ERP同步状态添加索引（用于查询待同步订单）
ALTER TABLE `order` ADD INDEX `idx_erp_sync_status` (`erp_sync_status`);

-- ============================================
-- 说明：
-- 1. jushuitan_config 表：存储聚水潭API配置信息
-- 2. order_sync_log 表：记录每次订单同步的详细日志
-- 3. order 表新增字段：
--    - erp_sync_status: 标记订单是否已同步到ERP
--    - erp_sync_time: 记录同步时间
--    - erp_order_id: 记录ERP系统中的订单ID
--    - seller_remark: 卖家备注（发货时可能需要）
-- ============================================
