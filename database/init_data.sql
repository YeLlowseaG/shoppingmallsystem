-- ============================================
-- 初始化基础数据脚本
-- ============================================

USE `chengren_shopping_mall`;

-- ============================================
-- 1. 初始化商品分类数据
-- ============================================

-- 一级分类
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(0, '情趣用品', 1, 1, 1),
(0, '健康护理', 1, 2, 1),
(0, '情趣内衣', 1, 3, 1),
(0, '其他', 1, 99, 1);

-- 获取一级分类ID（假设自增从1开始）
SET @category_level1_1 = LAST_INSERT_ID();
SET @category_level1_2 = @category_level1_1 + 1;
SET @category_level1_3 = @category_level1_1 + 2;
SET @category_level1_4 = @category_level1_1 + 3;

-- 二级分类（情趣用品）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_1, '男用器具', 2, 1, 1),
(@category_level1_1, '女用器具', 2, 2, 1),
(@category_level1_1, '润滑剂', 2, 3, 1),
(@category_level1_1, '安全套', 2, 4, 1);

-- 二级分类（健康护理）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_2, '护理用品', 2, 1, 1),
(@category_level1_2, '清洁用品', 2, 2, 1);

-- 二级分类（情趣内衣）
INSERT INTO `product_category` (`parent_id`, `category_name`, `level`, `sort_order`, `status`) VALUES
(@category_level1_3, '女士内衣', 2, 1, 1),
(@category_level1_3, '男士内衣', 2, 2, 1);

-- ============================================
-- 2. 初始化管理员用户（权限管理系统）
-- ============================================
-- 密码：admin123（BCrypt加密后的值）
-- 注意：实际使用时需要替换为真实的BCrypt加密密码
-- 可以使用在线工具或Java代码生成：BCrypt.hashpw("admin123", BCrypt.gensalt())
INSERT INTO `sys_admin_user` (`username`, `password`, `real_name`, `email`, `phone`, `status`) VALUES
('admin', '$2a$10$GYcMnv3gVVlUEl3fyNcRSesIAMtiajUc2s7puY0y4Msk1sCSOgVGi', '超级管理员', 'admin@shoppingmall.com', '13800138000', 1);

-- ============================================
-- 3. 初始化角色
-- ============================================
INSERT INTO `sys_role` (`role_code`, `role_name`, `description`, `status`, `sort_order`) VALUES
('ADMIN', '超级管理员', '拥有所有权限的超级管理员角色', 1, 1),
('OPERATOR', '运营人员', '负责商品管理、订单处理等运营工作', 1, 2),
('CUSTOMER_SERVICE', '客服人员', '负责订单处理、采购者服务等客服工作', 1, 3),
('FINANCE', '财务人员', '负责订单审核、财务统计等财务工作', 1, 4);

-- ============================================
-- 4. 初始化菜单（最多2层，只到页面层级）
-- ============================================

-- 4.1 一级菜单（目录，menu_type=0）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(0, '首页', 0, '/dashboard', 'Layout', 'HomeFilled', NULL, 1, 1),
(0, '商品管理', 0, '/product', 'Layout', 'Goods', NULL, 2, 1),
(0, '订单管理', 0, '/order', 'Layout', 'Document', NULL, 3, 1),
(0, '库存管理', 0, '/stock', 'Layout', 'Box', NULL, 4, 1),
(0, '采购者管理', 0, '/buyer', 'Layout', 'User', NULL, 5, 1),
(0, '营销管理', 0, '/marketing', 'Layout', 'Promotion', NULL, 6, 1),
(0, '数据统计', 0, '/statistics', 'Layout', 'DataAnalysis', NULL, 7, 1),
(0, '系统设置', 0, '/system', 'Layout', 'Setting', NULL, 8, 1),
(0, '权限管理', 0, '/permission', 'Layout', 'Lock', NULL, 9, 1);

-- 获取一级菜单ID
SET @menu_dashboard = (SELECT id FROM sys_menu WHERE menu_name = '首页' AND parent_id = 0);
SET @menu_product = (SELECT id FROM sys_menu WHERE menu_name = '商品管理' AND parent_id = 0);
SET @menu_order = (SELECT id FROM sys_menu WHERE menu_name = '订单管理' AND parent_id = 0);
SET @menu_stock = (SELECT id FROM sys_menu WHERE menu_name = '库存管理' AND parent_id = 0);
SET @menu_buyer = (SELECT id FROM sys_menu WHERE menu_name = '采购者管理' AND parent_id = 0);
SET @menu_marketing = (SELECT id FROM sys_menu WHERE menu_name = '营销管理' AND parent_id = 0);
SET @menu_statistics = (SELECT id FROM sys_menu WHERE menu_name = '数据统计' AND parent_id = 0);
SET @menu_system = (SELECT id FROM sys_menu WHERE menu_name = '系统设置' AND parent_id = 0);
SET @menu_permission = (SELECT id FROM sys_menu WHERE menu_name = '权限管理' AND parent_id = 0);

-- 4.2 二级菜单（页面，menu_type=1）
-- 首页
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_dashboard, '数据概览', 1, 'index', 'dashboard/Index', 'DataLine', 'admin:dashboard:view', 1, 1);

-- 商品管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_product, '商品列表', 1, 'list', 'product/List', 'List', 'admin:product:list', 1, 1),
(@menu_product, '商品发布', 1, 'add', 'product/Add', 'Plus', 'admin:product:add', 2, 1),
(@menu_product, '商品分类', 1, 'category', 'product/Category', 'Menu', 'admin:product:category', 3, 1);

-- 订单管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_order, '订单列表', 1, 'list', 'order/List', 'List', 'admin:order:list', 1, 1);

-- 库存管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_stock, '库存列表', 1, 'list', 'stock/List', 'List', 'admin:stock:list', 1, 1),
(@menu_stock, '库存预警', 1, 'warning', 'stock/Warning', 'Warning', 'admin:stock:warning', 2, 1),
(@menu_stock, '库存调整', 1, 'adjust', 'stock/Adjust', 'Edit', 'admin:stock:adjust', 3, 1),
(@menu_stock, '库存统计', 1, 'statistics', 'stock/Statistics', 'DataAnalysis', 'admin:stock:statistics', 4, 1);

-- 采购者管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_buyer, '采购者列表', 1, 'list', 'buyer/List', 'List', 'admin:buyer:list', 1, 1),
(@menu_buyer, '采购者审核', 1, 'audit', 'buyer/Audit', 'Check', 'admin:buyer:audit', 2, 1),
(@menu_buyer, '等级管理', 1, 'level', 'buyer/Level', 'Star', 'admin:buyer:level', 3, 1);

-- 营销管理
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_marketing, '促销活动', 1, 'promotion', 'marketing/Promotion', 'Promotion', 'admin:marketing:promotion', 1, 1),
(@menu_marketing, '价格策略', 1, 'price', 'marketing/Price', 'Money', 'admin:marketing:price', 2, 1);

-- 数据统计
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_statistics, '销售统计', 1, 'sales', 'statistics/Sales', 'TrendCharts', 'admin:statistics:sales', 1, 1),
(@menu_statistics, '订单统计', 1, 'order', 'statistics/Order', 'Document', 'admin:statistics:order', 2, 1),
(@menu_statistics, '商品统计', 1, 'product', 'statistics/Product', 'Goods', 'admin:statistics:product', 3, 1),
(@menu_statistics, '采购者统计', 1, 'buyer', 'statistics/Buyer', 'User', 'admin:statistics:buyer', 4, 1);

-- 系统设置（一级菜单下的二级菜单）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_system, '基础配置', 1, 'basic', 'system/Basic', 'Setting', 'admin:system:basic', 1, 1),
(@menu_system, '支付配置', 1, 'payment', 'system/Payment', 'CreditCard', 'admin:system:payment', 2, 1),
(@menu_system, '物流配置', 1, 'logistics', 'system/Logistics', 'Truck', 'admin:system:logistics', 3, 1),
(@menu_system, '通知设置', 1, 'notification', 'system/Notification', 'Bell', 'admin:system:notification', 4, 1);

-- 权限管理（一级菜单下的二级菜单）
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`) VALUES
(@menu_permission, '用户管理', 1, 'user', 'permission/User', 'User', 'admin:permission:user:list', 1, 1),
(@menu_permission, '角色管理', 1, 'role', 'permission/Role', 'UserFilled', 'admin:permission:role:list', 2, 1),
(@menu_permission, '菜单管理', 1, 'menu', 'permission/Menu', 'Menu', 'admin:permission:menu:list', 3, 1);

-- ============================================
-- 5. 分配管理员角色
-- ============================================
SET @admin_user_id = (SELECT id FROM sys_admin_user WHERE username = 'admin');
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'ADMIN');
INSERT INTO `sys_admin_role` (`admin_id`, `role_id`) VALUES
(@admin_user_id, @admin_role_id);

-- ============================================
-- 6. 分配角色菜单权限
-- ============================================

-- 6.1 超级管理员角色 - 拥有所有菜单权限
SET @admin_role_id = (SELECT id FROM sys_role WHERE role_code = 'ADMIN');
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @admin_role_id, id FROM sys_menu WHERE status = 1 AND deleted = 0;

-- 6.2 运营人员角色 - 商品、订单、库存、采购者、营销、数据统计权限
SET @operator_role_id = (SELECT id FROM sys_role WHERE role_code = 'OPERATOR');
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @operator_role_id, id FROM sys_menu 
WHERE (parent_id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '商品管理', '订单管理', '库存管理', '采购者管理', '营销管理', '数据统计')
) OR id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '商品管理', '订单管理', '库存管理', '采购者管理', '营销管理', '数据统计')
))
AND status = 1 AND deleted = 0;

-- 6.3 客服人员角色 - 订单、采购者权限
SET @cs_role_id = (SELECT id FROM sys_role WHERE role_code = 'CUSTOMER_SERVICE');
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @cs_role_id, id FROM sys_menu 
WHERE (parent_id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '订单管理', '采购者管理')
) OR id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '订单管理', '采购者管理')
))
AND status = 1 AND deleted = 0;

-- 6.4 财务人员角色 - 订单、数据统计权限
SET @finance_role_id = (SELECT id FROM sys_role WHERE role_code = 'FINANCE');
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT @finance_role_id, id FROM sys_menu 
WHERE (parent_id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '订单管理', '数据统计')
) OR id IN (
    SELECT id FROM sys_menu WHERE menu_name IN ('首页', '订单管理', '数据统计')
))
AND status = 1 AND deleted = 0;

-- ============================================
-- 初始化完成提示
-- ============================================
SELECT '基础数据初始化完成！' AS message;
SELECT '管理后台权限系统初始化完成！' AS permission_message;
SELECT '默认管理员账号：admin' AS username;
SELECT '默认管理员密码：admin123（请首次登录后修改）' AS password;
SELECT '超级管理员角色已分配所有权限' AS role_info;
SELECT '菜单层级：最多2层（一级目录 + 二级页面），权限只到页面层级' AS menu_structure;

