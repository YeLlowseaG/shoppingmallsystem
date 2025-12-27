-- ============================================
-- 更新脚本: update-20251227-add-warehouse-address-table.sql
-- 更新日期: 2025-12-27
-- 更新说明: 添加发货地址库表
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 创建发货地址库表
CREATE TABLE IF NOT EXISTS `warehouse_address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `warehouse_name` varchar(100) COLLATE utf8mb4_general_ci NOT NULL COMMENT '仓库名称',
  `contact_name` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系人姓名',
  `contact_phone` varchar(20) COLLATE utf8mb4_general_ci NOT NULL COMMENT '联系电话',
  `province` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '省份',
  `city` varchar(50) COLLATE utf8mb4_general_ci NOT NULL COMMENT '城市',
  `district` varchar(50) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '区县',
  `detail_address` varchar(200) COLLATE utf8mb4_general_ci NOT NULL COMMENT '详细地址',
  `zip_code` varchar(10) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认（0-否，1-是）',
  `status` tinyint DEFAULT '1' COMMENT '状态（0-禁用，1-启用）',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_default` (`is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='发货地址库表';

