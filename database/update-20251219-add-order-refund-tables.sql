USE chengren_shopping_mall;

-- ============================================
-- 订单退款功能 - 数据库表结构
-- ============================================
-- 说明：退款由管理员直接操作，无需用户申请和审核流程
-- ============================================

-- ============================================
-- 1. 订单退款记录表（order_refund）
-- ============================================
CREATE TABLE IF NOT EXISTS `order_refund` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_no` varchar(50) NOT NULL COMMENT '退款单号（唯一）',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `refund_amount` decimal(10,2) NOT NULL COMMENT '退款金额（商品金额，不含运费）',
  `refund_reason` varchar(500) DEFAULT NULL COMMENT '退款原因',
  `refund_status` tinyint NOT NULL DEFAULT '3' COMMENT '退款状态（3-退款中，4-退款成功，5-退款失败）',
  `refund_type` tinyint NOT NULL DEFAULT '1' COMMENT '退款类型（1-部分退款，2-全额退款）',
  `operator_id` bigint DEFAULT NULL COMMENT '操作人ID（管理员）',
  `operator_name` varchar(50) DEFAULT NULL COMMENT '操作人姓名',
  `operator_time` datetime DEFAULT NULL COMMENT '操作时间',
  `operator_remark` varchar(500) DEFAULT NULL COMMENT '操作备注',
  `refund_time` datetime DEFAULT NULL COMMENT '退款完成时间',
  `refund_payment_method` varchar(50) DEFAULT NULL COMMENT '退款支付方式（原支付方式）',
  `refund_payment_no` varchar(100) DEFAULT NULL COMMENT '退款支付单号',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_refund_no` (`refund_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_refund_status` (`refund_status`),
  KEY `idx_operator_id` (`operator_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单退款记录表';

-- ============================================
-- 2. 订单退款明细表（order_refund_item）
-- ============================================
CREATE TABLE IF NOT EXISTS `order_refund_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `refund_id` bigint NOT NULL COMMENT '退款申请ID',
  `order_item_id` bigint NOT NULL COMMENT '订单商品ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称（快照）',
  `product_code` varchar(50) DEFAULT NULL COMMENT '商品编码（快照）',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID（快照）',
  `spec_combination` varchar(500) DEFAULT NULL COMMENT 'SKU规格组合（快照）',
  `refund_quantity` int NOT NULL COMMENT '退款数量',
  `refund_price` decimal(10,2) NOT NULL COMMENT '退款单价（快照）',
  `refund_subtotal` decimal(10,2) NOT NULL COMMENT '退款小计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_refund_id` (`refund_id`),
  KEY `idx_order_item_id` (`order_item_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单退款明细表';

-- ============================================
-- 3. 在 order_item 表中添加已退款数量字段（可选）
-- ============================================
-- 说明：可以通过 order_refund_item 表统计已退款数量，也可以直接存储
-- 这里选择添加字段，方便查询
ALTER TABLE `order_item` 
ADD COLUMN `refunded_quantity` int NOT NULL DEFAULT '0' COMMENT '已退款数量' AFTER `quantity`;

-- ============================================
-- 4. 验证表结构
-- ============================================
DESCRIBE `order_refund`;
DESCRIBE `order_refund_item`;
DESCRIBE `order_item`;
