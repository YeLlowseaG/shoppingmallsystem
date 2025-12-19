/*
Navicat MySQL Data Transfer

Source Server         : mysql-test
Source Server Version : 80042
Source Host           : localhost:3306
Source Database       : chengren_shopping_mall

Target Server Type    : MYSQL
Target Server Version : 80042
File Encoding         : 65001

Date: 2025-12-19 10:22:20
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for announcement
-- ----------------------------
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告标题',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '公告内容（HTML格式，支持富文本和图片）',
  `images` text COLLATE utf8mb4_general_ci COMMENT '图片URL数组（JSON格式：["url1","url2"]）',
  `publish_date` date NOT NULL COMMENT '发布日期',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_publish_date` (`publish_date`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID（可为空，表示无规格或未选择）',
  `spec_combination` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格组合（JSON字符串，如 {"颜色":"红色","尺寸":"L"}）',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product_sku` (`user_id`,`product_id`,`sku_id`) COMMENT '用户、商品、SKU的唯一组合',
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=71 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- ----------------------------
-- Table structure for consultation
-- ----------------------------
DROP TABLE IF EXISTS `consultation`;
CREATE TABLE `consultation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（可为空，支持匿名咨询）',
  `contact_name` varchar(50) NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `consultation_content` text NOT NULL COMMENT '咨询内容',
  `reply_content` text COMMENT '回复内容',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待回复，1-已回复，2-已关闭',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `reply_admin_id` bigint DEFAULT NULL COMMENT '回复管理员ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购买咨询表';

-- ----------------------------
-- Table structure for help_article
-- ----------------------------
DROP TABLE IF EXISTS `help_article`;
CREATE TABLE `help_article` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '文章标题',
  `content` longtext COLLATE utf8mb4_general_ci NOT NULL COMMENT '文章内容（HTML格式，支持富文本和图片）',
  `images` text COLLATE utf8mb4_general_ci COMMENT '图片URL数组（JSON格式：["url1","url2"]）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帮助中心文章表';

-- ----------------------------
-- Table structure for help_category
-- ----------------------------
DROP TABLE IF EXISTS `help_category`;
CREATE TABLE `help_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID（0表示顶级分类）',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint(1) NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort` (`sort`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='帮助中心分类表';

-- ----------------------------
-- Table structure for logistics_company
-- ----------------------------
DROP TABLE IF EXISTS `logistics_company`;
CREATE TABLE `logistics_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物流公司编码（唯一）',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '物流公司名称',
  `company_short_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流公司简称',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `website` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '官网地址',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物流公司表';

-- ----------------------------
-- Table structure for member_level
-- ----------------------------
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '等级名称（如：普通会员、银卡会员、金卡会员、钻石会员）',
  `min_points` int NOT NULL DEFAULT '0' COMMENT '最低积分（包含）',
  `max_points` int DEFAULT NULL COMMENT '最高积分（不包含，NULL表示无上限）',
  `discount_rate` decimal(5,2) NOT NULL DEFAULT '100.00' COMMENT '折扣率（如：95.00表示95折，100.00表示无折扣）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序号（数字越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '等级描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_points_range` (`min_points`,`max_points`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员等级表';

-- ----------------------------
-- Table structure for message
-- ----------------------------
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` bigint DEFAULT NULL COMMENT '发送人ID（NULL表示系统消息）',
  `receiver_id` bigint NOT NULL COMMENT '接收人ID',
  `title` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息标题',
  `content` text COLLATE utf8mb4_general_ci NOT NULL COMMENT '消息内容',
  `message_type` tinyint DEFAULT '0' COMMENT '消息类型（0-普通，1-系统，2-订单，3-其他）',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读（0-未读，1-已读）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_message_type` (`message_type`),
  KEY `idx_receiver_read_time` (`receiver_id`,`is_read`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内消息表';

-- ----------------------------
-- Table structure for navigation_menu
-- ----------------------------
DROP TABLE IF EXISTS `navigation_menu`;
CREATE TABLE `navigation_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `menu_name` varchar(100) NOT NULL COMMENT '菜单名称',
  `menu_url` varchar(200) NOT NULL COMMENT '菜单链接',
  `menu_type` varchar(50) NOT NULL DEFAULT 'link' COMMENT '菜单类型：link-直接链接，category-分类，brand-品牌，type-类型',
  `menu_params` varchar(500) DEFAULT NULL COMMENT '菜单参数，JSON格式存储',
  `icon` varchar(100) DEFAULT NULL COMMENT '菜单图标',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `target` varchar(20) DEFAULT '_self' COMMENT '打开方式：_self-当前窗口，_blank-新窗口',
  `description` varchar(500) DEFAULT NULL COMMENT '菜单描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='导航菜单配置表';

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '订单号（唯一）',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `shipping_fee` decimal(10,2) DEFAULT '0.00' COMMENT '运费',
  `tax` decimal(10,2) DEFAULT '0.00' COMMENT '税金',
  `actual_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `order_status` tinyint DEFAULT '0' COMMENT '订单状态（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）',
  `payment_method` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）',
  `payment_status` tinyint DEFAULT '0' COMMENT '支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）',
  `shipping_address` json NOT NULL COMMENT '收货地址（JSON格式）',
  `order_remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '订单备注',
  `delivery_date` date DEFAULT NULL COMMENT '配送日期',
  `delivery_time` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送时间段',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_user_status_time` (`user_id`,`order_status`,`create_time`),
  KEY `idx_status_time` (`order_status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ----------------------------
-- Table structure for order_item
-- ----------------------------
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称（快照）',
  `product_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '商品图片（快照）',
  `product_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品编码（快照）',
  `price` decimal(10,2) NOT NULL COMMENT '单价（快照）',
  `quantity` int NOT NULL COMMENT '数量',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计金额',
  `weight` decimal(10,2) DEFAULT '0.00' COMMENT '商品重量（kg）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `sku_id` bigint DEFAULT NULL COMMENT 'SKU ID（快照）',
  `spec_combination` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT 'SKU规格组合（快照）',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品表';

-- ----------------------------
-- Table structure for order_logistics
-- ----------------------------
DROP TABLE IF EXISTS `order_logistics`;
CREATE TABLE `order_logistics` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `logistics_company` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流公司',
  `logistics_no` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流单号',
  `shipping_time` datetime DEFAULT NULL COMMENT '发货时间',
  `tracking_info` json DEFAULT NULL COMMENT '物流跟踪信息（JSON数组）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单物流表';

-- ----------------------------
-- Table structure for order_message
-- ----------------------------
DROP TABLE IF EXISTS `order_message`;
CREATE TABLE `order_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `message_type` tinyint NOT NULL COMMENT '消息类型：1-我已付款，2-我有问题',
  `title` varchar(200) DEFAULT NULL COMMENT '问题标题（我有问题时必填）',
  `content` text COMMENT '问题内容（我有问题时必填）',
  `payment_amount` decimal(10,2) DEFAULT NULL COMMENT '付款金额（我已付款时必填）',
  `payment_method` varchar(100) DEFAULT NULL COMMENT '付款方式（我已付款时必填）',
  `payment_date` date DEFAULT NULL COMMENT '付款日期（我已付款时必填）',
  `payment_time` time DEFAULT NULL COMMENT '付款时间（我已付款时必填）',
  `remarks` text COMMENT '备注',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '处理状态：0-待处理，1-处理中，2-已处理，3-已关闭',
  `handler_id` bigint DEFAULT NULL COMMENT '处理人ID（管理员）',
  `handler_name` varchar(50) DEFAULT NULL COMMENT '处理人姓名',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handle_remark` text COMMENT '处理备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单问题/消息表';

-- ----------------------------
-- Table structure for out_of_stock_registration
-- ----------------------------
DROP TABLE IF EXISTS `out_of_stock_registration`;
CREATE TABLE `out_of_stock_registration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `notify_when_available` tinyint DEFAULT '1' COMMENT '到货通知（0-否，1-是）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='缺货登记表';

-- ----------------------------
-- Table structure for password_reset_code
-- ----------------------------
DROP TABLE IF EXISTS `password_reset_code`;
CREATE TABLE `password_reset_code` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名',
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱地址',
  `code` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '验证码/重置令牌',
  `code_type` varchar(20) COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'RESET_PASSWORD' COMMENT '验证码类型（RESET_PASSWORD-密码重置）',
  `email_content` text COLLATE utf8mb4_general_ci COMMENT '发送的邮件内容',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-未使用，1-已使用，2-已过期）',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_code` (`code`),
  KEY `idx_email` (`email`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_user_code` (`user_id`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='密码重置验证码记录表';

-- ----------------------------
-- Table structure for payment_record
-- ----------------------------
DROP TABLE IF EXISTS `payment_record`;
CREATE TABLE `payment_record` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `payment_no` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付流水号（唯一）',
  `payment_method` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）',
  `amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `refunded_amount` decimal(10,2) DEFAULT '0.00' COMMENT '已退款金额',
  `payment_status` tinyint DEFAULT '0' COMMENT '支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款原因',
  `refund_operator_id` bigint DEFAULT NULL COMMENT '退款操作人ID（管理员）',
  `refund_operator_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '退款操作人姓名',
  `callback_data` json DEFAULT NULL COMMENT '回调数据（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_refunded_amount` (`refunded_amount`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- ----------------------------
-- Table structure for pre_deposit
-- ----------------------------
DROP TABLE IF EXISTS `pre_deposit`;
CREATE TABLE `pre_deposit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '余额',
  `available_balance` decimal(10,2) DEFAULT '0.00' COMMENT '可用余额',
  `frozen_balance` decimal(10,2) DEFAULT '0.00' COMMENT '冻结余额',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款表';

-- ----------------------------
-- Table structure for pre_deposit_detail
-- ----------------------------
DROP TABLE IF EXISTS `pre_deposit_detail`;
CREATE TABLE `pre_deposit_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
  `deposit_amount` decimal(10,2) DEFAULT '0.00' COMMENT '存入金额',
  `expense_amount` decimal(10,2) DEFAULT '0.00' COMMENT '支出金额',
  `frozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '冻结金额',
  `unfrozen_amount` decimal(10,2) DEFAULT '0.00' COMMENT '解冻金额',
  `current_balance` decimal(10,2) DEFAULT '0.00' COMMENT '当前余额',
  `available_balance` decimal(10,2) DEFAULT '0.00' COMMENT '可用余额',
  `event` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '事件描述（如：预存款支付、在线充值、预存款退款、代充值）',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `order_id` bigint DEFAULT NULL COMMENT '关联订单ID（如果是订单相关操作）',
  `order_no` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '关联订单号',
  `external_trade_no` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '外部交易号（微信/支付宝返回的交易号）',
  `internal_order_no` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '内部订单号（用于充值记录查找）',
  `type` tinyint NOT NULL COMMENT '类型（1-充值，2-消费，3-退款）',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-待审核，1-已通过，2-已拒绝）',
  `payment_method` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付方式',
  `payment_voucher` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付凭证URL',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_user_type_time` (`user_id`,`type`,`create_time`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_event` (`event`),
  KEY `idx_user_event_time` (`user_id`,`event`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款明细表';

-- ----------------------------
-- Table structure for product
-- ----------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品编码/SKU（唯一）',
  `product_name` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '商品名称',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `main_image` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '主图URL',
  `images` json DEFAULT NULL COMMENT '商品图片（JSON数组）',
  `description` text COLLATE utf8mb4_general_ci COMMENT '商品描述',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础批发价',
  `stock` int DEFAULT '0' COMMENT '库存数量',
  `sales_count` int DEFAULT '0' COMMENT '销量',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-下架，1-上架）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `sale_price` decimal(10,2) DEFAULT NULL COMMENT '销售价格',
  `suggested_retail_price` decimal(10,2) DEFAULT NULL COMMENT '建议零售价',
  `market_retail_price` decimal(10,2) DEFAULT NULL COMMENT '市场零售价',
  `warning_stock` int DEFAULT NULL COMMENT '警戒库存',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '商品重量(g)',
  `brand_id` bigint DEFAULT NULL COMMENT '品牌ID（关联website_brand表）',
  `enable_spec` tinyint(1) DEFAULT '0' COMMENT '是否启用规格：0-否，1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sales_count` (`sales_count`),
  KEY `idx_category_status_sales` (`category_id`,`status`,`sales_count`)
) ENGINE=InnoDB AUTO_INCREMENT=49 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- ----------------------------
-- Table structure for product_category
-- ----------------------------
DROP TABLE IF EXISTS `product_category`;
CREATE TABLE `product_category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID（0表示顶级分类）',
  `category_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '分类名称',
  `level` tinyint NOT NULL COMMENT '分类级别（1-一级，2-二级，3-三级）',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- ----------------------------
-- Table structure for product_favorite
-- ----------------------------
DROP TABLE IF EXISTS `product_favorite`;
CREATE TABLE `product_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品收藏表';

-- ----------------------------
-- Table structure for product_price
-- ----------------------------
DROP TABLE IF EXISTS `product_price`;
CREATE TABLE `product_price` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_level` tinyint NOT NULL COMMENT '用户等级（0-普通，1-VIP，2-金牌）',
  `price` decimal(10,2) NOT NULL COMMENT '等级价格',
  `min_quantity` int DEFAULT '1' COMMENT '最小数量（用于阶梯价格）',
  `max_quantity` int DEFAULT NULL COMMENT '最大数量（NULL表示无上限）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_level` (`user_level`),
  KEY `idx_product_level` (`product_id`,`user_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品价格表';

-- ----------------------------
-- Table structure for product_review
-- ----------------------------
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` tinyint NOT NULL COMMENT '评分：1-5星',
  `review_content` text COMMENT '评价内容',
  `review_images` json DEFAULT NULL COMMENT '评价图片JSON数组',
  `admin_reply` text COMMENT '管理员回复',
  `status` tinyint DEFAULT '0' COMMENT '状态：0-待审核，1-已通过，2-已拒绝',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_rating` (`rating`),
  KEY `idx_create_time` (`create_time`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';

-- ----------------------------
-- Table structure for product_sku
-- ----------------------------
DROP TABLE IF EXISTS `product_sku`;
CREATE TABLE `product_sku` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `sku_code` varchar(100) NOT NULL COMMENT 'SKU编码（唯一）',
  `spec_combination` json NOT NULL COMMENT '规格组合JSON（如：{"颜色":"红色","尺寸":"L"}）',
  `price` decimal(10,2) NOT NULL COMMENT 'SKU价格',
  `stock` int DEFAULT '0' COMMENT 'SKU库存',
  `warning_stock` int DEFAULT '0' COMMENT 'SKU警戒库存',
  `sales_count` int DEFAULT '0' COMMENT 'SKU销量',
  `weight` decimal(10,2) DEFAULT NULL COMMENT 'SKU重量(g)',
  `sku_image` varchar(500) DEFAULT NULL COMMENT 'SKU主图（可选）',
  `sku_images` json DEFAULT NULL COMMENT 'SKU图片列表（可选）',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_price` (`price`),
  KEY `idx_stock` (`stock`),
  KEY `idx_status` (`status`),
  CONSTRAINT `fk_sku_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=65 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品SKU表';

-- ----------------------------
-- Table structure for product_spec_key
-- ----------------------------
DROP TABLE IF EXISTS `product_spec_key`;
CREATE TABLE `product_spec_key` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `spec_name` varchar(50) NOT NULL COMMENT '规格名称（如：颜色、尺寸、容量）',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_spec_key_product` FOREIGN KEY (`product_id`) REFERENCES `product` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=48 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格属性表';

-- ----------------------------
-- Table structure for product_spec_value
-- ----------------------------
DROP TABLE IF EXISTS `product_spec_value`;
CREATE TABLE `product_spec_value` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `spec_key_id` bigint NOT NULL COMMENT '规格属性ID',
  `spec_value` varchar(100) NOT NULL COMMENT '规格值（如：红色、L码、500ml）',
  `spec_image` varchar(500) DEFAULT NULL COMMENT '规格图片URL（可选）',
  `sort_order` int DEFAULT '0' COMMENT '排序权重',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_spec_key_id` (`spec_key_id`),
  KEY `idx_sort_order` (`sort_order`),
  CONSTRAINT `fk_spec_value_key` FOREIGN KEY (`spec_key_id`) REFERENCES `product_spec_key` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格值表';

-- ----------------------------
-- Table structure for product_stock
-- ----------------------------
DROP TABLE IF EXISTS `product_stock`;
CREATE TABLE `product_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `available_stock` int DEFAULT '0' COMMENT '可用库存',
  `locked_stock` int DEFAULT '0' COMMENT '锁定库存（下单锁定）',
  `total_stock` int DEFAULT '0' COMMENT '总库存',
  `warning_threshold` int DEFAULT '10' COMMENT '预警阈值',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品库存表';

-- ----------------------------
-- Table structure for region
-- ----------------------------
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `code` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '地区编码（如：110000）',
  `name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '地区名称',
  `parent_id` bigint DEFAULT NULL COMMENT '父级地区ID（NULL表示顶级）',
  `level` tinyint NOT NULL COMMENT '级别（1-省/直辖市，2-市，3-区/县）',
  `sort_order` int DEFAULT '0' COMMENT '排序顺序',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`),
  KEY `idx_parent_level` (`parent_id`,`level`) COMMENT '复合索引：按父级和级别查询'
) ENGINE=InnoDB AUTO_INCREMENT=3367 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区表';

-- ----------------------------
-- Table structure for shipping_method
-- ----------------------------
DROP TABLE IF EXISTS `shipping_method`;
CREATE TABLE `shipping_method` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `method_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配送方式编码（唯一）',
  `method_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '配送方式名称',
  `logistics_company_id` bigint DEFAULT NULL COMMENT '关联物流公司ID（可为空，如上门自提）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送方式描述',
  `shipping_template_id` bigint DEFAULT NULL COMMENT '关联运费模板ID',
  `base_price` decimal(10,2) DEFAULT '0.00' COMMENT '基础运费（固定运费时使用）',
  `calculation_type` tinyint DEFAULT '1' COMMENT '运费计算方式（1-固定运费，2-按重量，3-按件数，4-按金额，5-运费模板）',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_method_code` (`method_code`),
  KEY `idx_logistics_company_id` (`logistics_company_id`),
  KEY `idx_shipping_template_id` (`shipping_template_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送方式表';

-- ----------------------------
-- Table structure for shipping_rule
-- ----------------------------
DROP TABLE IF EXISTS `shipping_rule`;
CREATE TABLE `shipping_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '运费模板ID',
  `region_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区编码（省/市/区，为空表示默认规则）',
  `region_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区名称（省/市/区，为空表示默认规则）',
  `first_weight` decimal(10,2) DEFAULT '0.00' COMMENT '首重（单位：kg，按件数时表示首件）',
  `first_price` decimal(10,2) DEFAULT '0.00' COMMENT '首重价格（按件数时表示首件价格）',
  `continue_weight` decimal(10,2) DEFAULT '0.00' COMMENT '续重（单位：kg，按件数时表示续件）',
  `continue_price` decimal(10,2) DEFAULT '0.00' COMMENT '续重价格（按件数时表示续件价格）',
  `free_shipping_amount` decimal(10,2) DEFAULT '0.00' COMMENT '包邮金额（订单金额达到此金额时免运费）',
  `free_shipping_weight` decimal(10,2) DEFAULT '0.00' COMMENT '包邮重量（订单重量达到此重量时免运费，单位：kg）',
  `free_shipping_quantity` int DEFAULT '0' COMMENT '包邮件数（订单件数达到此数量时免运费）',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_id` (`template_id`),
  KEY `idx_region_code` (`region_code`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运费规则表';

-- ----------------------------
-- Table structure for shipping_template
-- ----------------------------
DROP TABLE IF EXISTS `shipping_template`;
CREATE TABLE `shipping_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '运费模板名称',
  `calculation_type` tinyint NOT NULL DEFAULT '1' COMMENT '计算方式（1-按重量，2-按件数，3-按金额）',
  `free_shipping_amount` decimal(10,2) DEFAULT '0.00' COMMENT '包邮金额（订单金额达到此金额时免运费）',
  `free_shipping_weight` decimal(10,2) DEFAULT '0.00' COMMENT '包邮重量（订单重量达到此重量时免运费，单位：kg）',
  `free_shipping_quantity` int DEFAULT '0' COMMENT '包邮件数（订单件数达到此数量时免运费）',
  `default_first_weight` decimal(10,2) DEFAULT '0.00' COMMENT '默认首重（单位：kg）',
  `default_first_price` decimal(10,2) DEFAULT '0.00' COMMENT '默认首重价格',
  `default_continue_weight` decimal(10,2) DEFAULT '0.00' COMMENT '默认续重（单位：kg）',
  `default_continue_price` decimal(10,2) DEFAULT '0.00' COMMENT '默认续重价格',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模板描述',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运费模板表';

-- ----------------------------
-- Table structure for stock_notification
-- ----------------------------
DROP TABLE IF EXISTS `stock_notification`;
CREATE TABLE `stock_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（冗余字段，避免商品删除后无法显示）',
  `product_code` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品编码（冗余字段）',
  `main_image` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图（冗余字段）',
  `base_price` decimal(10,2) DEFAULT NULL COMMENT '商品价格（冗余字段）',
  `contact_phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '联系邮箱',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态（0-待通知，1-已通知，2-已取消）',
  `notify_type` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT 'email' COMMENT '通知方式（email-邮箱，sms-短信，both-两种）',
  `notified_at` datetime DEFAULT NULL COMMENT '通知时间',
  `expired_at` datetime DEFAULT NULL COMMENT '登记过期时间',
  `remark` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='缺货登记表';

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text COMMENT '配置值',
  `config_name` varchar(200) NOT NULL COMMENT '配置名称',
  `config_desc` varchar(500) DEFAULT NULL COMMENT '配置描述',
  `config_type` varchar(50) NOT NULL DEFAULT 'text' COMMENT '配置类型：text/textarea/image/number',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint DEFAULT '1' COMMENT '状态：0-禁用，1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统配置表';

-- ----------------------------
-- Table structure for sys_admin_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_role`;
CREATE TABLE `sys_admin_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` bigint NOT NULL COMMENT '管理员ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`,`role_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_admin_create` (`admin_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员角色关联表';

-- ----------------------------
-- Table structure for sys_admin_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_admin_user`;
CREATE TABLE `sys_admin_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名（唯一）',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '最后登录IP',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator_id` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员用户表';

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父菜单ID（0表示顶级菜单）',
  `menu_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '菜单名称',
  `menu_type` tinyint NOT NULL COMMENT '菜单类型（0-目录，1-菜单，2-按钮）',
  `path` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '路由路径',
  `component` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '组件路径',
  `icon` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '图标',
  `permission` varchar(100) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '权限标识（如：admin:user:list）',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_permission` (`permission`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_status` (`status`),
  KEY `idx_parent_status_sort` (`parent_id`,`status`,`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=57 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色编码（唯一，如：ADMIN、OPERATOR）',
  `role_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '角色名称（如：超级管理员、运营人员）',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '角色描述',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`,`menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`),
  KEY `idx_role_create` (`role_id`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1205 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名（唯一）',
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `payment_password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '支付密码（BCrypt加密）',
  `real_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `gender` tinyint DEFAULT NULL COMMENT '性别（0-女，1-男）',
  `birthday` date DEFAULT NULL COMMENT '出生日期',
  `phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '手机号',
  `fixed_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '固定电话',
  `operator` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '运营人员',
  `region` json DEFAULT NULL COMMENT '地区（JSON格式：{"province":"省","city":"市","district":"区"}）',
  `address` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `zip_code` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮编',
  `security_question` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '安全问题',
  `security_answer` varchar(255) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '安全问题答案',
  `wangwang` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '旺旺账号',
  `is_member` tinyint NOT NULL DEFAULT '0' COMMENT '是否会员（0-普通用户，1-会员）',
  `member_level_id` bigint DEFAULT NULL COMMENT '会员等级ID（关联 member_level 表，普通用户为 NULL）',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-待审核，1-已激活，2-已禁用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_is_member` (`is_member`),
  KEY `idx_member_level_id` (`member_level_id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- Table structure for sys_user_audit
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_audit`;
CREATE TABLE `sys_user_audit` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `audit_status` tinyint NOT NULL DEFAULT '0' COMMENT '审核状态（0-待审核，1-已通过，2-已拒绝）',
  `audit_comment` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '审核意见',
  `auditor_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `audit_time` datetime DEFAULT NULL COMMENT '审核时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户审核表';

-- ----------------------------
-- Table structure for user_address
-- ----------------------------
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人电话（固定电话）',
  `receiver_mobile` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货人手机',
  `province` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '省份',
  `city` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '城市',
  `district` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '区县',
  `detail_address` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '详细地址',
  `zip_code` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址（0-否，1-是）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';

-- ----------------------------
-- Table structure for website_advertisement
-- ----------------------------
DROP TABLE IF EXISTS `website_advertisement`;
CREATE TABLE `website_advertisement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `ad_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '广告名称',
  `ad_position` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '广告位置（floor_1/floor_2.../brand_side_1/brand_side_2）',
  `image_url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片URL',
  `link_type` tinyint NOT NULL DEFAULT '0' COMMENT '链接类型（0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接）',
  `link_value` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '链接值',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_position` (`ad_position`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='广告位表';

-- ----------------------------
-- Table structure for website_banner
-- ----------------------------
DROP TABLE IF EXISTS `website_banner`;
CREATE TABLE `website_banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `title` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '轮播图标题',
  `image_url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT '图片URL',
  `link_type` tinyint NOT NULL DEFAULT '0' COMMENT '链接类型（0-无链接，1-商品分类，2-商品详情，3-促销活动，4-外部链接）',
  `link_value` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '链接值（根据link_type不同，存储categoryId/productId/promotionId/url）',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='轮播图表';

-- ----------------------------
-- Table structure for website_brand
-- ----------------------------
DROP TABLE IF EXISTS `website_brand`;
CREATE TABLE `website_brand` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `brand_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '品牌名称',
  `logo_url` varchar(500) COLLATE utf8mb4_general_ci NOT NULL COMMENT 'Logo图片URL',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '品牌描述',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='品牌表';
