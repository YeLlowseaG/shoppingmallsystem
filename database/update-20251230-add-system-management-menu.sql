-- ============================================
-- 更新脚本: update-20251230-add-system-management-menu.sql
-- 更新日期: 2025-12-30
-- 更新说明: 添加系统管理菜单及其子菜单（支付接口日志查询、定时任务管理）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 添加系统管理一级菜单（菜单ID使用62）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (62, 0, '系统管理', 0, 'system', NULL, 'Setting', NULL, 100, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`);

-- 添加支付接口日志查询二级菜单（菜单ID使用63，父菜单ID为62）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (63, 62, '支付接口日志查询', 1, 'payment-api-log', 'system/PaymentApiLog', 'Document', 'system:payment-api-log:view', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 添加定时任务管理二级菜单（菜单ID使用64，父菜单ID为62）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (64, 62, '定时任务管理', 1, 'scheduled-task', 'system/ScheduledTask', 'Timer', 'system:scheduled-task:view', 2, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 为超级管理员角色分配系统管理菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 62
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 62);

-- 为超级管理员角色分配支付接口日志查询菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 63
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 63);

-- 为超级管理员角色分配定时任务管理菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 64
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 64);

-- 说明：
-- - 系统管理一级菜单ID: 62 (parent_id=0)
-- - 支付接口日志查询二级菜单ID: 63 (parent_id=62)
-- - 定时任务管理二级菜单ID: 64 (parent_id=62)
-- - 菜单路径: /admin/system/payment-api-log 和 /admin/system/scheduled-task
-- - 组件路径: system/PaymentApiLog 和 system/ScheduledTask
-- - 权限标识: system:payment-api-log:view 和 system:scheduled-task:view

