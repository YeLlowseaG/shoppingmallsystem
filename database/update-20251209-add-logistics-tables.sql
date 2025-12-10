-- ============================================
-- 更新脚本: update-20251209-add-logistics-tables.sql
-- 更新日期: 2025-12-09
-- 更新说明: 添加物流管理相关表（物流公司表、配送方式表、运费模板表、运费规则表）
-- 作者: ShoppingMall Team
-- ============================================

-- 1. 创建物流公司表
CREATE TABLE IF NOT EXISTS `logistics_company` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物流公司编码（唯一）',
  `company_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '物流公司名称',
  `company_short_name` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物流公司简称',
  `contact_phone` varchar(20) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '联系电话',
  `website` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '官网地址',
  `sort_order` int DEFAULT '0' COMMENT '排序（数字越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_company_code` (`company_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='物流公司表';

-- 2. 创建配送方式表
CREATE TABLE IF NOT EXISTS `shipping_method` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `method_code` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配送方式编码（唯一）',
  `method_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '配送方式名称',
  `logistics_company_id` bigint DEFAULT NULL COMMENT '关联物流公司ID（可为空，如上门自提）',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '配送方式描述',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='配送方式表';

-- 3. 创建运费模板表
CREATE TABLE IF NOT EXISTS `shipping_template` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '运费模板名称',
  `calculation_type` tinyint NOT NULL DEFAULT '1' COMMENT '计算方式（1-按重量，2-按件数，3-按金额）',
  `free_shipping_amount` decimal(10,2) DEFAULT '0.00' COMMENT '包邮金额（订单金额达到此金额时免运费）',
  `free_shipping_weight` decimal(10,2) DEFAULT '0.00' COMMENT '包邮重量（订单重量达到此重量时免运费，单位：kg）',
  `free_shipping_quantity` int DEFAULT '0' COMMENT '包邮件数（订单件数达到此数量时免运费）',
  `default_first_weight` decimal(10,2) DEFAULT '0.00' COMMENT '默认首重（单位：kg）',
  `default_first_price` decimal(10,2) DEFAULT '0.00' COMMENT '默认首重价格',
  `default_continue_weight` decimal(10,2) DEFAULT '0.00' COMMENT '默认续重（单位：kg）',
  `default_continue_price` decimal(10,2) DEFAULT '0.00' COMMENT '默认续重价格',
  `description` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '模板描述',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运费模板表';

-- 4. 创建运费规则表
CREATE TABLE IF NOT EXISTS `shipping_rule` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint NOT NULL COMMENT '运费模板ID',
  `region_code` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区编码（省/市/区，为空表示默认规则）',
  `region_name` varchar(200) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '地区名称（省/市/区，为空表示默认规则）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='运费规则表';

-- 5. 初始化物流公司数据
INSERT INTO `logistics_company` (`company_code`, `company_name`, `company_short_name`, `contact_phone`, `website`, `sort_order`, `status`) VALUES
('pickup', '上门自提', '自提', NULL, NULL, 1, 1),
('anneng', '安能物流', '安能', NULL, NULL, 2, 1),
('yto', '圆通快递', '圆通', NULL, 'http://www.yto.net.cn', 3, 1),
('sto', '申通快递', '申通', NULL, 'http://www.sto.cn', 4, 1),
('debon', '德邦快递', '德邦', NULL, 'http://www.deppon.com', 5, 1),
('sf', '顺丰快递', '顺丰', NULL, 'http://www.sf-express.com', 6, 1)
ON DUPLICATE KEY UPDATE `company_name` = VALUES(`company_name`);

-- 6. 初始化配送方式数据
INSERT INTO `shipping_method` (`method_code`, `method_name`, `logistics_company_id`, `description`, `base_price`, `calculation_type`, `sort_order`, `status`) VALUES
('pickup', '上门自提', 1, '需要到仓库自提,不发货', 0.00, 1, 1, 1),
('anneng', '安能物流', 2, '安能物流主要适合货物重量在10KG以上非包邮订单,广东省内最低40元/票,省外最低70元/票,紧急订单或偏远地区订单请询问后选择。', 70.00, 1, 2, 1),
('yto_cainiao', '圆通菜鸟', 3, '', 3.40, 1, 3, 1),
('yto_pdd', '圆通拼多多', 3, '', 3.40, 1, 4, 1),
('yto_jd', '圆通京东', 3, '', 3.40, 1, 5, 1),
('sto_pdd', '申通拼多多', 4, '', 3.40, 1, 6, 1),
('sto_cainiao', '申通菜鸟', 4, '', 3.40, 1, 7, 1),
('sto_jd', '申通京东', 4, '', 3.40, 1, 8, 1),
('debon', '德邦快递', 5, '适用于3KG以上订单.', 13.00, 1, 9, 1),
('sf', '顺丰快递', 6, '下单前请自行去顺丰http://www.sf-express.com/cn/sc/查询该地是否到达!超区快递不派送需要转其他快递的费用,自行承担', 18.00, 1, 10, 1),
('freight_collect', '运费到付', NULL, '运费到付仅支持顺丰到付,安能物流到付', 0.00, 1, 11, 1),
('free_shipping', '包邮订单', NULL, '2025年10月份货物订单金额符合相应区域包邮政策要求的方可选择"包邮订单"', 0.00, 1, 12, 1)
ON DUPLICATE KEY UPDATE `method_name` = VALUES(`method_name`);

-- 7. 初始化运费模板数据
INSERT INTO `shipping_template` (`template_name`, `calculation_type`, `free_shipping_amount`, `free_shipping_weight`, `free_shipping_quantity`, `default_first_weight`, `default_first_price`, `default_continue_weight`, `default_continue_price`, `description`, `status`) VALUES
('标准按重量计费模板', 1, 99.00, 0.00, 0, 1.00, 8.00, 1.00, 3.00, '标准按重量计费：首重1kg 8元，续重1kg 3元，满99元包邮', 1),
('标准按件数计费模板', 2, 88.00, 0.00, 0, 1.00, 6.00, 1.00, 2.00, '标准按件数计费：首件6元，续件2元，满88元包邮', 1),
('标准按金额计费模板', 3, 100.00, 0.00, 0, 0.00, 10.00, 0.00, 0.00, '标准按金额计费：订单金额的10%作为运费，满100元包邮', 1),
('偏远地区按重量模板', 1, 199.00, 0.00, 0, 1.00, 15.00, 1.00, 8.00, '偏远地区按重量计费：首重1kg 15元，续重1kg 8元，满199元包邮', 1)
ON DUPLICATE KEY UPDATE `template_name` = VALUES(`template_name`);

-- 8. 初始化运费规则数据
-- 注意：template_id使用硬编码值，对应上面插入的4个模板（按插入顺序）
-- 如果模板已存在且ID不同，需要先查询模板ID并调整下面的template_id值
-- 或者使用子查询：SELECT id FROM shipping_template WHERE template_name = '标准按重量计费模板'

-- 标准按重量计费模板的规则（对应第一个模板）
INSERT INTO `shipping_rule` (`template_id`, `region_code`, `region_name`, `first_weight`, `first_price`, `continue_weight`, `continue_price`, `free_shipping_amount`, `free_shipping_weight`, `free_shipping_quantity`, `sort_order`) VALUES
-- 默认规则
(1, NULL, '默认规则', 1.00, 8.00, 1.00, 3.00, 99.00, 0.00, 0, 0),
-- 广东省内
(1, '440000', '广东省', 1.00, 6.00, 1.00, 2.00, 88.00, 0.00, 0, 1),
(1, '440100', '广东省-广州市', 1.00, 5.00, 1.00, 2.00, 79.00, 0.00, 0, 2),
(1, '440300', '广东省-深圳市', 1.00, 5.00, 1.00, 2.00, 79.00, 0.00, 0, 3),
-- 江浙沪
(1, '320000', '江苏省', 1.00, 7.00, 1.00, 2.50, 99.00, 0.00, 0, 4),
(1, '330000', '浙江省', 1.00, 7.00, 1.00, 2.50, 99.00, 0.00, 0, 5),
(1, '310000', '上海市', 1.00, 7.00, 1.00, 2.50, 99.00, 0.00, 0, 6),
-- 偏远地区
(1, '650000', '新疆维吾尔自治区', 1.00, 20.00, 1.00, 12.00, 299.00, 0.00, 0, 7),
(1, '540000', '西藏自治区', 1.00, 25.00, 1.00, 15.00, 399.00, 0.00, 0, 8),
(1, '630000', '青海省', 1.00, 18.00, 1.00, 10.00, 299.00, 0.00, 0, 9)
ON DUPLICATE KEY UPDATE `first_price` = VALUES(`first_price`);

-- 标准按件数计费模板的规则（template_id = 2）
INSERT INTO `shipping_rule` (`template_id`, `region_code`, `region_name`, `first_weight`, `first_price`, `continue_weight`, `continue_price`, `free_shipping_amount`, `free_shipping_weight`, `free_shipping_quantity`, `sort_order`) VALUES
-- 默认规则
(2, NULL, '默认规则', 1.00, 6.00, 1.00, 2.00, 88.00, 0.00, 0, 0),
-- 广东省内
(2, '440000', '广东省', 1.00, 5.00, 1.00, 1.50, 79.00, 0.00, 0, 1),
(2, '440100', '广东省-广州市', 1.00, 4.00, 1.00, 1.50, 69.00, 0.00, 0, 2),
(2, '440300', '广东省-深圳市', 1.00, 4.00, 1.00, 1.50, 69.00, 0.00, 0, 3),
-- 江浙沪
(2, '320000', '江苏省', 1.00, 5.50, 1.00, 2.00, 88.00, 0.00, 0, 4),
(2, '330000', '浙江省', 1.00, 5.50, 1.00, 2.00, 88.00, 0.00, 0, 5),
(2, '310000', '上海市', 1.00, 5.50, 1.00, 2.00, 88.00, 0.00, 0, 6)
ON DUPLICATE KEY UPDATE `first_price` = VALUES(`first_price`);

-- 标准按金额计费模板的规则（template_id = 3）
INSERT INTO `shipping_rule` (`template_id`, `region_code`, `region_name`, `first_weight`, `first_price`, `continue_weight`, `continue_price`, `free_shipping_amount`, `free_shipping_weight`, `free_shipping_quantity`, `sort_order`) VALUES
-- 默认规则（按金额的10%计算，这里first_price表示费率百分比）
(3, NULL, '默认规则', 0.00, 10.00, 0.00, 0.00, 100.00, 0.00, 0, 0),
-- 广东省内（按金额的8%计算）
(3, '440000', '广东省', 0.00, 8.00, 0.00, 0.00, 88.00, 0.00, 0, 1),
(3, '440100', '广东省-广州市', 0.00, 7.00, 0.00, 0.00, 79.00, 0.00, 0, 2),
(3, '440300', '广东省-深圳市', 0.00, 7.00, 0.00, 0.00, 79.00, 0.00, 0, 3),
-- 江浙沪（按金额的9%计算）
(3, '320000', '江苏省', 0.00, 9.00, 0.00, 0.00, 99.00, 0.00, 0, 4),
(3, '330000', '浙江省', 0.00, 9.00, 0.00, 0.00, 99.00, 0.00, 0, 5),
(3, '310000', '上海市', 0.00, 9.00, 0.00, 0.00, 99.00, 0.00, 0, 6)
ON DUPLICATE KEY UPDATE `first_price` = VALUES(`first_price`);

-- 偏远地区按重量模板的规则（template_id = 4）
INSERT INTO `shipping_rule` (`template_id`, `region_code`, `region_name`, `first_weight`, `first_price`, `continue_weight`, `continue_price`, `free_shipping_amount`, `free_shipping_weight`, `free_shipping_quantity`, `sort_order`) VALUES
-- 默认规则
(4, NULL, '默认规则', 1.00, 15.00, 1.00, 8.00, 199.00, 0.00, 0, 0),
-- 新疆
(4, '650000', '新疆维吾尔自治区', 1.00, 25.00, 1.00, 15.00, 399.00, 0.00, 0, 1),
(4, '650100', '新疆维吾尔自治区-乌鲁木齐市', 1.00, 22.00, 1.00, 12.00, 299.00, 0.00, 0, 2),
-- 西藏
(4, '540000', '西藏自治区', 1.00, 30.00, 1.00, 18.00, 499.00, 0.00, 0, 3),
(4, '540100', '西藏自治区-拉萨市', 1.00, 28.00, 1.00, 16.00, 399.00, 0.00, 0, 4),
-- 青海
(4, '630000', '青海省', 1.00, 20.00, 1.00, 12.00, 299.00, 0.00, 0, 5),
(4, '630100', '青海省-西宁市', 1.00, 18.00, 1.00, 10.00, 299.00, 0.00, 0, 6),
-- 内蒙古
(4, '150000', '内蒙古自治区', 1.00, 18.00, 1.00, 10.00, 299.00, 0.00, 0, 7),
(4, '150100', '内蒙古自治区-呼和浩特市', 1.00, 16.00, 1.00, 9.00, 249.00, 0.00, 0, 8)
ON DUPLICATE KEY UPDATE `first_price` = VALUES(`first_price`);

