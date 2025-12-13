-- ============================================
-- 更新脚本: update-20251213-create-member-level-table.sql
-- 更新日期: 2025-12-13
-- 更新说明: 创建会员等级表，支持动态配置会员等级（为后续积分、折扣功能做支撑）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 重命名采购者相关菜单为会员相关菜单
update sys_menu a set a.menu_name='会员管理' where a.menu_name='采购者管理';
update sys_menu a set a.menu_name='会员列表' where a.menu_name='采购者列表';

-- 创建会员等级表
CREATE TABLE IF NOT EXISTS `member_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称（如：普通会员、银卡会员、金卡会员、钻石会员）',
  `min_points` int NOT NULL DEFAULT 0 COMMENT '最低积分（包含）',
  `max_points` int DEFAULT NULL COMMENT '最高积分（不包含，NULL表示无上限）',
  `discount_rate` decimal(5,2) NOT NULL DEFAULT 100.00 COMMENT '折扣率（如：95.00表示95折，100.00表示无折扣）',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序号（数字越小越靠前）',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态（0-禁用，1-启用）',
  `description` varchar(500) DEFAULT NULL COMMENT '等级描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`),
  KEY `idx_sort_order` (`sort_order`),
  KEY `idx_points_range` (`min_points`, `max_points`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='会员等级表';

-- 插入默认会员等级数据
INSERT INTO `member_level` (`level_name`, `min_points`, `max_points`, `discount_rate`, `sort_order`, `status`, `description`) VALUES
('普通会员', 0, 1000, 100.00, 1, 1, '新注册会员，享受基础价格'),
('银卡会员', 1000, 5000, 98.00, 2, 1, '累计积分达到1000分，享受98折优惠'),
('金卡会员', 5000, 20000, 95.00, 3, 1, '累计积分达到5000分，享受95折优惠'),
('钻石会员', 20000, NULL, 90.00, 4, 1, '累计积分达到20000分，享受9折优惠');
