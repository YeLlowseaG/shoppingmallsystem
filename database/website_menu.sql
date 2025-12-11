-- ============================================
-- Website模块菜单配置
-- 添加到"系统设置"菜单下
-- ============================================

USE `chengren_shopping_mall`;

-- 插入菜单数据（放在系统设置 id=8 下）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `path`, `component`, `menu_type`, `sort_order`, `permission`, `icon`, `status`, `deleted`) VALUES
-- 轮播图管理
(8, '轮播图管理', 'banner', 'website/Banner', 1, 4, 'admin:website:banner:list', 'Picture', 1, 0),
-- 品牌管理
(8, '品牌管理', 'brand', 'website/Brand', 1, 5, 'admin:website:brand:list', 'ShoppingBag', 1, 0),
-- 广告位管理
(8, '广告位管理', 'advertisement', 'website/Advertisement', 1, 6, 'admin:website:advertisement:list', 'Postcard', 1, 0);

-- 查看插入结果
SELECT id, parent_id, menu_name, path, component, permission FROM sys_menu WHERE parent_id = 8 ORDER BY sort_order;
