/*
Navicat MySQL Data Transfer

Source Server         : mysql-test
Source Server Version : 80042
Source Host           : localhost:3306
Source Database       : chengren_shopping_mall

Target Server Type    : MYSQL
Target Server Version : 80042
File Encoding         : 65001

Date: 2025-12-09 13:01:30
*/
-- 创建数据库
CREATE DATABASE IF NOT EXISTS `chengren_shopping_mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `chengren_shopping_mall`;

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- ----------------------------
-- Records of cart
-- ----------------------------

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
-- Records of message
-- ----------------------------

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
  `payment_status` tinyint DEFAULT '0' COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- ----------------------------
-- Records of order
-- ----------------------------

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
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品表';

-- ----------------------------
-- Records of order_item
-- ----------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单物流表';

-- ----------------------------
-- Records of order_logistics
-- ----------------------------

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
-- Records of out_of_stock_registration
-- ----------------------------

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
  `payment_status` tinyint DEFAULT '0' COMMENT '支付状态（0-待支付，1-已支付，2-已退款，3-已失败）',
  `payment_time` datetime DEFAULT NULL COMMENT '支付时间',
  `callback_data` json DEFAULT NULL COMMENT '回调数据（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- ----------------------------
-- Records of payment_record
-- ----------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款表';

-- ----------------------------
-- Records of pre_deposit
-- ----------------------------

-- ----------------------------
-- Table structure for pre_deposit_detail
-- ----------------------------
DROP TABLE IF EXISTS `pre_deposit_detail`;
CREATE TABLE `pre_deposit_detail` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '金额',
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
  KEY `idx_user_type_time` (`user_id`,`type`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款明细表';

-- ----------------------------
-- Records of pre_deposit_detail
-- ----------------------------

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
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sales_count` (`sales_count`),
  KEY `idx_category_status_sales` (`category_id`,`status`,`sales_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- ----------------------------
-- Records of product
-- ----------------------------

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
-- Records of product_category
-- ----------------------------
INSERT INTO `product_category` VALUES ('1', '0', '情趣用品', '1', '1', '1', '0', '2025-12-05 13:44:55', '2025-12-05 13:44:55');
INSERT INTO `product_category` VALUES ('2', '0', '健康护理', '1', '2', '1', '0', '2025-12-05 13:44:55', '2025-12-05 13:44:55');
INSERT INTO `product_category` VALUES ('3', '0', '情趣内衣', '1', '3', '1', '0', '2025-12-05 13:44:55', '2025-12-05 13:44:55');
INSERT INTO `product_category` VALUES ('4', '0', '其他', '1', '99', '1', '0', '2025-12-05 13:44:55', '2025-12-05 13:44:55');
INSERT INTO `product_category` VALUES ('5', '1', '男用器具', '2', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('6', '1', '女用器具', '2', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('7', '1', '润滑剂', '2', '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('8', '1', '安全套', '2', '4', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('9', '2', '护理用品', '2', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('10', '2', '清洁用品', '2', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('11', '3', '女士内衣', '2', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `product_category` VALUES ('12', '3', '男士内衣', '2', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品收藏表';

-- ----------------------------
-- Records of product_favorite
-- ----------------------------

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
-- Records of product_price
-- ----------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品库存表';

-- ----------------------------
-- Records of product_stock
-- ----------------------------

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
-- Records of sys_admin_role
-- ----------------------------
INSERT INTO `sys_admin_role` VALUES ('4', '1', '1', '2025-12-05 14:59:37');

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
-- Records of sys_admin_user
-- ----------------------------
INSERT INTO `sys_admin_user` VALUES ('1', 'admin', '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi', '超级管理员', 'admin@shoppingmall.com', '13800138000', '1', '2025-12-08 10:03:53', '127.0.0.1', '0', '2025-12-05 13:44:56', '2025-12-05 14:09:55', null);

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
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES ('1', '0', '首页', '0', '/dashboard', 'Layout', 'HomeFilled', null, '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('2', '0', '商品管理', '0', '/product', 'Layout', 'Goods', null, '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('3', '0', '订单管理', '0', '/order', 'Layout', 'Document', null, '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('4', '0', '库存管理', '0', '/stock', 'Layout', 'Box', null, '4', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('5', '0', '采购者管理', '0', '/buyer', 'Layout', 'User', null, '5', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('6', '0', '营销管理', '0', '/marketing', 'Layout', 'Promotion', null, '6', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('7', '0', '数据统计', '0', '/statistics', 'Layout', 'DataAnalysis', null, '7', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('8', '0', '系统设置', '0', '/system', 'Layout', 'Setting', null, '8', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('9', '0', '权限管理', '0', '/permission', 'Layout', 'Lock', null, '9', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('10', '1', '数据概览', '1', 'index', 'dashboard/Index', 'DataLine', 'admin:dashboard:view', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('11', '2', '商品列表', '1', 'list', 'product/List', 'List', 'admin:product:list', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('12', '2', '商品发布', '1', 'add', 'product/Add', 'Plus', 'admin:product:add', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('13', '2', '商品分类', '1', 'category', 'product/Category', 'Menu', 'admin:product:category', '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('14', '3', '订单列表', '1', 'list', 'order/List', 'List', 'admin:order:list', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('15', '4', '库存列表', '1', 'list', 'stock/List', 'List', 'admin:stock:list', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('16', '4', '库存预警', '1', 'warning', 'stock/Warning', 'Warning', 'admin:stock:warning', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('17', '4', '库存调整', '1', 'adjust', 'stock/Adjust', 'Edit', 'admin:stock:adjust', '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('18', '4', '库存统计', '1', 'statistics', 'stock/Statistics', 'DataAnalysis', 'admin:stock:statistics', '4', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('19', '5', '采购者列表', '1', 'list', 'buyer/List', 'List', 'admin:buyer:list', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('20', '5', '采购者审核', '1', 'audit', 'buyer/Audit', 'Check', 'admin:buyer:audit', '2', '0', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('21', '5', '等级管理', '1', 'level', 'buyer/Level', 'Star', 'admin:buyer:level', '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('22', '6', '促销活动', '1', 'promotion', 'marketing/Promotion', 'Promotion', 'admin:marketing:promotion', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('23', '6', '价格策略', '1', 'price', 'marketing/Price', 'Money', 'admin:marketing:price', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('24', '7', '销售统计', '1', 'sales', 'statistics/Sales', 'TrendCharts', 'admin:statistics:sales', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('25', '7', '订单统计', '1', 'order', 'statistics/Order', 'Document', 'admin:statistics:order', '2', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('26', '7', '商品统计', '1', 'product', 'statistics/Product', 'Goods', 'admin:statistics:product', '3', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('27', '7', '采购者统计', '1', 'buyer', 'statistics/Buyer', 'User', 'admin:statistics:buyer', '4', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_menu` VALUES ('28', '8', '基础配置', '1', 'basic', 'system/Basic', 'Setting', 'admin:system:basic', '1', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('29', '8', '支付配置', '1', 'payment', 'system/Payment', 'CreditCard', 'admin:system:payment', '2', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('30', '8', '物流配置', '1', 'logistics', 'system/Logistics', 'Truck', 'admin:system:logistics', '3', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('31', '8', '通知设置', '1', 'notification', 'system/Notification', 'Bell', 'admin:system:notification', '4', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('32', '9', '用户管理', '1', 'user', 'permission/User', 'User', 'admin:permission:user:list', '1', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('33', '9', '角色管理', '1', 'role', 'permission/Role', 'UserFilled', 'admin:permission:role:list', '2', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');
INSERT INTO `sys_menu` VALUES ('34', '9', '菜单管理', '1', 'menu', 'permission/Menu', 'Menu', 'admin:permission:menu:list', '3', '1', '0', '2025-12-05 13:44:57', '2025-12-05 13:44:57');

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
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES ('1', 'ADMIN', '超级管理员', '拥有所有权限的超级管理员角色', '1', '1', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_role` VALUES ('2', 'OPERATOR', '运营人员', '负责商品管理、订单处理等运营工作', '1', '2', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_role` VALUES ('3', 'CUSTOMER_SERVICE', '客服人员', '负责订单处理、采购者服务等客服工作', '1', '3', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');
INSERT INTO `sys_role` VALUES ('4', 'FINANCE', '财务人员', '负责订单审核、财务统计等财务工作', '1', '4', '0', '2025-12-05 13:44:56', '2025-12-05 13:44:56');

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
) ENGINE=InnoDB AUTO_INCREMENT=226 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES ('147', '3', '1', '2025-12-08 10:01:22');
INSERT INTO `sys_role_menu` VALUES ('148', '3', '10', '2025-12-08 10:01:22');
INSERT INTO `sys_role_menu` VALUES ('149', '3', '3', '2025-12-08 10:01:22');
INSERT INTO `sys_role_menu` VALUES ('150', '3', '14', '2025-12-08 10:01:22');
INSERT INTO `sys_role_menu` VALUES ('151', '4', '1', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('152', '4', '10', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('153', '4', '2', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('154', '4', '11', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('155', '4', '12', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('156', '4', '13', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('157', '4', '3', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('158', '4', '14', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('159', '4', '4', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('160', '4', '15', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('161', '4', '16', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('162', '4', '17', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('163', '4', '18', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('164', '4', '6', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('165', '4', '22', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('166', '4', '23', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('167', '4', '7', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('168', '4', '24', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('169', '4', '25', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('170', '4', '26', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('171', '4', '27', '2025-12-08 10:01:26');
INSERT INTO `sys_role_menu` VALUES ('172', '2', '1', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('173', '2', '10', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('174', '2', '2', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('175', '2', '11', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('176', '2', '12', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('177', '2', '13', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('178', '2', '3', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('179', '2', '14', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('180', '2', '4', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('181', '2', '15', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('182', '2', '16', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('183', '2', '17', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('184', '2', '18', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('185', '2', '6', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('186', '2', '22', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('187', '2', '23', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('188', '2', '7', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('189', '2', '24', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('190', '2', '25', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('191', '2', '26', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('192', '2', '27', '2025-12-08 10:01:30');
INSERT INTO `sys_role_menu` VALUES ('193', '1', '1', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('194', '1', '10', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('195', '1', '2', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('196', '1', '11', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('197', '1', '12', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('198', '1', '13', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('199', '1', '3', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('200', '1', '14', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('201', '1', '4', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('202', '1', '15', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('203', '1', '16', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('204', '1', '17', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('205', '1', '18', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('206', '1', '19', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('207', '1', '21', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('208', '1', '6', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('209', '1', '22', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('210', '1', '23', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('211', '1', '7', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('212', '1', '24', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('213', '1', '25', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('214', '1', '26', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('215', '1', '27', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('216', '1', '8', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('217', '1', '28', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('218', '1', '29', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('219', '1', '30', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('220', '1', '31', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('221', '1', '9', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('222', '1', '32', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('223', '1', '33', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('224', '1', '34', '2025-12-08 10:01:37');
INSERT INTO `sys_role_menu` VALUES ('225', '1', '5', '2025-12-08 10:01:37');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '用户名（唯一）',
  `email` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '邮箱',
  `password` varchar(255) COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码（BCrypt加密）',
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
  `user_level` tinyint DEFAULT '0' COMMENT '用户等级（0-普通，1-VIP，2-金牌）',
  `status` tinyint DEFAULT '0' COMMENT '状态（0-待审核，1-已激活，2-已禁用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_user_level` (`user_level`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES ('1', 'buyer1', 'buyer1@test.com', '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi', '测试采购者1', '1', null, '13800138001', null, null, '{\"city\": \"深圳市\", \"district\": \"南山区\", \"province\": \"广东省\"}', '科技园南区', null, null, null, null, '0', '1', '0', '2025-12-05 13:45:19', '2025-12-05 13:45:19');
INSERT INTO `sys_user` VALUES ('2', 'buyer2', 'buyer2@test.com', '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi', '测试采购者2', '0', null, '13800138002', null, null, '{\"city\": \"北京市\", \"district\": \"朝阳区\", \"province\": \"北京市\"}', '建国路88号', null, null, null, null, '1', '1', '0', '2025-12-05 13:45:19', '2025-12-05 13:45:19');
INSERT INTO `sys_user` VALUES ('3', 'test1', '234324@qq.com', '$2a$10$FivV5r/nNGJitQ938wmpqOdMHwjqX.Do0KVI68OFuF7VpLzKXxeAa', '土豆一', '1', null, '18000000099', null, null, '{\"city\": \"北京市\", \"district\": \"东城区\", \"province\": \"北京\"}', '测试地址3233333', null, null, null, null, '0', '1', '0', '2025-12-05 15:54:16', '2025-12-05 15:54:16');
INSERT INTO `sys_user` VALUES ('4', 'user', '43543534@qq.com', '$2a$10$XK8/GUiN1BXOYm8G5HfV.eyUjWbH2cNznR9wvzKstwcjqfQc0/WKu', '土豆二', '0', null, '15667777777', null, null, '{\"city\": \"上海市\", \"district\": \"徐汇区\", \"province\": \"上海\"}', '测试地址333', null, null, null, null, '1', '1', '0', '2025-12-08 08:38:44', '2025-12-08 08:38:44');
INSERT INTO `sys_user` VALUES ('5', 'user1', '3423423400@qq.com', '$2a$10$bqGBzNq9mbiHGonYIeVfgu8t0BTlqOtIMNQi/h4z5apv6iNz/RQdy', '张三丰', '1', '2020-06-05', '15666777777', '020-2344324200', '运营3修改33', '{\"city\": \"上海市\", \"district\": \"徐汇区\", \"province\": \"上海\"}', '测试地址修改', '544400', '安全问题修改11', '答案33修1133', 'wagnwagn修改33', '0', '1', '0', '2025-12-08 08:45:55', '2025-12-08 08:45:55');
INSERT INTO `sys_user` VALUES ('6', 'user5', '2342342@qq.com', '$2a$10$Pg7cDO09NrIxKblE/3cx4uoBmhd1nPkF1bGdlK91aY/9QzzF24RAS', '张三44', '1', null, '15666666666', '', '测试', '{\"city\": \"广州市\", \"district\": \"天河区\", \"province\": \"广东\"}', '测试地址', '', '', '', '', '0', '1', '0', '2025-12-08 10:08:01', '2025-12-08 10:08:01');

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
-- Records of sys_user_audit
-- ----------------------------
INSERT INTO `sys_user_audit` VALUES ('1', '1', '1', '测试用户，自动通过审核', null, '2025-12-05 13:45:19', '2025-12-05 13:45:19');
INSERT INTO `sys_user_audit` VALUES ('2', '2', '1', '测试用户，自动通过审核', null, '2025-12-05 13:45:19', '2025-12-05 13:45:19');
INSERT INTO `sys_user_audit` VALUES ('3', '3', '1', '审核通过！', '1', '2025-12-05 16:00:43', '2025-12-05 15:54:16');
INSERT INTO `sys_user_audit` VALUES ('4', '4', '1', '审核通过11', '1', '2025-12-08 08:43:01', '2025-12-08 08:38:44');
INSERT INTO `sys_user_audit` VALUES ('5', '5', '1', '', '1', '2025-12-08 10:02:06', '2025-12-08 08:45:55');

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
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';

-- ----------------------------
-- Records of user_address
-- ----------------------------
INSERT INTO `user_address` VALUES ('1', '5', '张大大修改', '020-233333300', '15666666600', '北京', '北京市', '丰台区', '测试地址修改', '514400', '1', '0', '2025-12-08 16:37:51', '2025-12-08 16:37:51');
INSERT INTO `user_address` VALUES ('2', '5', '刘德华', null, '16778888888', '陕西', '西安市', '新城区', '测试地址33', '544444', '0', '0', '2025-12-08 16:39:28', '2025-12-08 16:39:28');
