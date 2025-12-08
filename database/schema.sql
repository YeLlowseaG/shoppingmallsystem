-- ============================================
-- B2B成人用品采购系统数据库建表脚本
-- 数据库名: chengren_shopping_mall
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_general_ci
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `chengren_shopping_mall` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `chengren_shopping_mall`;

-- ============================================
-- 1. 用户相关表
-- ============================================

-- 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `gender` TINYINT DEFAULT NULL COMMENT '性别（0-女，1-男）',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `region` JSON DEFAULT NULL COMMENT '地区（JSON格式：{"province":"省","city":"市","district":"区"}）',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `user_level` TINYINT DEFAULT 0 COMMENT '用户等级（0-普通，1-VIP，2-金牌）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0-待审核，1-已激活，2-已禁用）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_phone` (`phone`),
  KEY `idx_status` (`status`),
  KEY `idx_user_level` (`user_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 用户审核表
CREATE TABLE IF NOT EXISTS `sys_user_audit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `audit_status` TINYINT NOT NULL DEFAULT 0 COMMENT '审核状态（0-待审核，1-已通过，2-已拒绝）',
  `audit_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `auditor_id` BIGINT DEFAULT NULL COMMENT '审核人ID',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_audit_status` (`audit_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户审核表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS `user_address` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `zip_code` VARCHAR(10) DEFAULT NULL COMMENT '邮编',
  `is_default` TINYINT DEFAULT 0 COMMENT '是否默认地址（0-否，1-是）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='收货地址表';

-- ============================================
-- 2. 商品相关表
-- ============================================

-- 商品分类表
CREATE TABLE IF NOT EXISTS `product_category` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID（0表示顶级分类）',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `level` TINYINT NOT NULL COMMENT '分类级别（1-一级，2-二级，3-三级）',
  `sort_order` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_level` (`level`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码/SKU（唯一）',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `category_id` BIGINT NOT NULL COMMENT '分类ID',
  `main_image` VARCHAR(500) DEFAULT NULL COMMENT '主图URL',
  `images` JSON DEFAULT NULL COMMENT '商品图片（JSON数组）',
  `description` TEXT COMMENT '商品描述',
  `base_price` DECIMAL(10,2) NOT NULL COMMENT '基础批发价',
  `stock` INT DEFAULT 0 COMMENT '库存数量',
  `sales_count` INT DEFAULT 0 COMMENT '销量',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0-下架，1-上架）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_code` (`product_code`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_sales_count` (`sales_count`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品表';

-- 商品价格表
CREATE TABLE IF NOT EXISTS `product_price` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `user_level` TINYINT NOT NULL COMMENT '用户等级（0-普通，1-VIP，2-金牌）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '等级价格',
  `min_quantity` INT DEFAULT 1 COMMENT '最小数量（用于阶梯价格）',
  `max_quantity` INT DEFAULT NULL COMMENT '最大数量（NULL表示无上限）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_level` (`user_level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品价格表';

-- 商品库存表
CREATE TABLE IF NOT EXISTS `product_stock` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `available_stock` INT DEFAULT 0 COMMENT '可用库存',
  `locked_stock` INT DEFAULT 0 COMMENT '锁定库存（下单锁定）',
  `total_stock` INT DEFAULT 0 COMMENT '总库存',
  `warning_threshold` INT DEFAULT 10 COMMENT '预警阈值',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品库存表';

-- ============================================
-- 3. 订单相关表
-- ============================================

-- 订单表
CREATE TABLE IF NOT EXISTS `order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单号（唯一）',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `shipping_fee` DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
  `tax` DECIMAL(10,2) DEFAULT 0.00 COMMENT '税金',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `order_status` TINYINT DEFAULT 0 COMMENT '订单状态（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）',
  `payment_method` VARCHAR(20) DEFAULT NULL COMMENT '支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）',
  `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态（0-未支付，1-已支付，2-已退款）',
  `shipping_address` JSON NOT NULL COMMENT '收货地址（JSON格式）',
  `order_remark` VARCHAR(500) DEFAULT NULL COMMENT '订单备注',
  `delivery_date` DATE DEFAULT NULL COMMENT '配送日期',
  `delivery_time` VARCHAR(50) DEFAULT NULL COMMENT '配送时间段',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_status` (`order_status`),
  KEY `idx_payment_status` (`payment_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单表';

-- 订单商品表
CREATE TABLE IF NOT EXISTS `order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
  `product_image` VARCHAR(500) DEFAULT NULL COMMENT '商品图片（快照）',
  `product_code` VARCHAR(50) NOT NULL COMMENT '商品编码（快照）',
  `price` DECIMAL(10,2) NOT NULL COMMENT '单价（快照）',
  `quantity` INT NOT NULL COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `weight` DECIMAL(10,2) DEFAULT 0.00 COMMENT '商品重量（kg）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单商品表';

-- 订单物流表
CREATE TABLE IF NOT EXISTS `order_logistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `logistics_company` VARCHAR(100) DEFAULT NULL COMMENT '物流公司',
  `logistics_no` VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
  `shipping_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `tracking_info` JSON DEFAULT NULL COMMENT '物流跟踪信息（JSON数组）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_id` (`order_id`),
  KEY `idx_logistics_no` (`logistics_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='订单物流表';

-- ============================================
-- 4. 购物车表
-- ============================================

-- 购物车表
CREATE TABLE IF NOT EXISTS `cart` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='购物车表';

-- ============================================
-- 5. 支付相关表
-- ============================================

-- 预存款表
CREATE TABLE IF NOT EXISTS `pre_deposit` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '余额',
  `available_balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '可用余额',
  `frozen_balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结余额',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款表';

-- 预存款明细表
CREATE TABLE IF NOT EXISTS `pre_deposit_detail` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `type` TINYINT NOT NULL COMMENT '类型（1-充值，2-消费，3-退款）',
  `status` TINYINT DEFAULT 0 COMMENT '状态（0-待审核，1-已通过，2-已拒绝）',
  `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式',
  `payment_voucher` VARCHAR(500) DEFAULT NULL COMMENT '支付凭证URL',
  `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预存款明细表';

-- 支付记录表
CREATE TABLE IF NOT EXISTS `payment_record` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `payment_no` VARCHAR(100) NOT NULL COMMENT '支付流水号（唯一）',
  `payment_method` VARCHAR(20) NOT NULL COMMENT '支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '支付金额',
  `payment_status` TINYINT DEFAULT 0 COMMENT '支付状态（0-待支付，1-已支付，2-已退款，3-已失败）',
  `payment_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `callback_data` JSON DEFAULT NULL COMMENT '回调数据（JSON格式）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_payment_status` (`payment_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- ============================================
-- 6. 其他表
-- ============================================

-- 站内消息表
CREATE TABLE IF NOT EXISTS `message` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `sender_id` BIGINT DEFAULT NULL COMMENT '发送人ID（NULL表示系统消息）',
  `receiver_id` BIGINT NOT NULL COMMENT '接收人ID',
  `title` VARCHAR(200) NOT NULL COMMENT '消息标题',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT DEFAULT 0 COMMENT '消息类型（0-普通，1-系统，2-订单，3-其他）',
  `is_read` TINYINT DEFAULT 0 COMMENT '是否已读（0-未读，1-已读）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='站内消息表';

-- 商品收藏表
CREATE TABLE IF NOT EXISTS `product_favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='商品收藏表';

-- 缺货登记表
CREATE TABLE IF NOT EXISTS `out_of_stock_registration` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `notify_when_available` TINYINT DEFAULT 1 COMMENT '到货通知（0-否，1-是）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='缺货登记表';

-- ============================================
-- 7. 权限管理相关表
-- ============================================

-- 管理员用户表
CREATE TABLE IF NOT EXISTS `sys_admin_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名（唯一）',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `creator_id` BIGINT DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码（唯一，如：ADMIN、OPERATOR）',
  `role_name` VARCHAR(100) NOT NULL COMMENT '角色名称（如：超级管理员、运营人员）',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '角色描述',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `sort_order` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 菜单表
CREATE TABLE IF NOT EXISTS `sys_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `parent_id` BIGINT DEFAULT 0 COMMENT '父菜单ID（0表示顶级菜单）',
  `menu_name` VARCHAR(100) NOT NULL COMMENT '菜单名称',
  `menu_type` TINYINT NOT NULL COMMENT '菜单类型（0-目录，1-菜单，2-按钮）',
  `path` VARCHAR(200) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(200) DEFAULT NULL COMMENT '组件路径',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '图标',
  `permission` VARCHAR(100) DEFAULT NULL COMMENT '权限标识（如：admin:user:list）',
  `sort_order` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
  `status` TINYINT DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`menu_type`),
  KEY `idx_status` (`status`),
  UNIQUE KEY `uk_permission` (`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- 管理员角色关联表
CREATE TABLE IF NOT EXISTS `sys_admin_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_role` (`admin_id`, `role_id`),
  KEY `idx_admin_id` (`admin_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='管理员角色关联表';

-- 角色菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- ============================================
-- 8. 索引优化（复合索引）
-- ============================================

-- 订单表复合索引（用于订单查询和统计）
ALTER TABLE `order` ADD INDEX `idx_user_status_time` (`user_id`, `order_status`, `create_time`);
ALTER TABLE `order` ADD INDEX `idx_status_time` (`order_status`, `create_time`);

-- 商品表复合索引（用于商品列表查询）
ALTER TABLE `product` ADD INDEX `idx_category_status_sales` (`category_id`, `status`, `sales_count`);

-- 消息表复合索引（用于消息查询）
ALTER TABLE `message` ADD INDEX `idx_receiver_read_time` (`receiver_id`, `is_read`, `create_time`);

-- 预存款明细表复合索引（用于明细查询）
ALTER TABLE `pre_deposit_detail` ADD INDEX `idx_user_type_time` (`user_id`, `type`, `create_time`);

-- 商品价格表复合索引（用于价格查询）
ALTER TABLE `product_price` ADD INDEX `idx_product_level` (`product_id`, `user_level`);

-- 权限管理表复合索引
-- 菜单表复合索引（用于菜单树查询）
ALTER TABLE `sys_menu` ADD INDEX `idx_parent_status_sort` (`parent_id`, `status`, `sort_order`);

-- 管理员角色关联表复合索引（用于查询管理员的所有角色）
ALTER TABLE `sys_admin_role` ADD INDEX `idx_admin_create` (`admin_id`, `create_time`);

-- 角色菜单关联表复合索引（用于查询角色的所有菜单）
ALTER TABLE `sys_role_menu` ADD INDEX `idx_role_create` (`role_id`, `create_time`);
