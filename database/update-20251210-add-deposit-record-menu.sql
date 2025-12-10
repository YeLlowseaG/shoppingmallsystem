-- 添加预存款交易记录管理菜单
-- 更新脚本名称: update-20251210-add-deposit-record-menu.sql
-- 更新日期: 2025-12-10
-- 更新说明: 在采购者管理下添加预存款交易记录管理菜单
-- 作者: ShoppingMall Team

-- 1. 添加"预存款交易记录"二级菜单（放在采购者管理下）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (43, 5, '预存款交易记录', 1, 'deposit', 'deposit/Record', 'Money', 'admin:deposit:list', 4, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `component` = VALUES(`component`), `permission` = VALUES(`permission`);

-- 2. 为超级管理员角色分配菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 43
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 42);

-- 3. 为运营人员角色分配菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 43
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 42)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

