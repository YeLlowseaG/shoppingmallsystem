-- ============================================
-- 更新脚本: update-20251213-add-finance-menu.sql
-- 更新日期: 2025-12-13
-- 更新说明: 添加财务管理模块菜单和权限（只包含支付记录管理，预存款充值退款功能在原有的预存款交易记录页面中）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 先将预存款交易记录菜单的父级改为财务管理（如果该菜单已存在）
update sys_menu a set a.parent_id=51 where a.menu_name='预存款交易记录';

-- 1. 添加"财务管理"顶级菜单（目录）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (51, 0, '财务管理', 0, '/finance', 'Layout', 'Money', NULL, 10, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`);

-- 2. 添加"支付记录管理"子菜单（放在财务管理下）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (52, 51, '支付记录', 1, 'payment-record', 'finance/PaymentRecord', 'CreditCard', 'admin:finance:payment:list', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 3. 为超级管理员角色分配财务管理菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 51
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 51);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 52
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 52);

-- 4. 为财务人员角色分配财务管理菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, 51
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 4 AND `menu_id` = 51)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 4);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 4, 52
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 4 AND `menu_id` = 52)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 4);

-- 5. 为运营人员角色分配财务管理菜单权限（如果存在，只读权限）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 51
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 51)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 52
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 52)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);
