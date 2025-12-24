-- ============================================
-- 更新脚本: update-20251224-add-erp-menu.sql
-- 更新日期: 2025-12-24
-- 更新说明: 添加ERP管理模块菜单和权限
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 1. 添加"ERP管理"顶级菜单（目录）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (59, 0, 'ERP管理', 0, '/erp', 'Layout', 'Connection', NULL, 11, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `sort_order` = VALUES(`sort_order`);

-- 2. 添加"聚水潭配置"子菜单（放在ERP管理下）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (60, 59, '聚水潭配置', 1, 'config', 'erp/Config', 'Setting', 'admin:erp:config:manage', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 3. 添加"订单同步日志"子菜单（放在ERP管理下）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (61, 59, '订单同步日志', 1, 'order-sync', 'erp/OrderSync', 'Document', 'admin:erp:sync:list', 2, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 4. 为超级管理员角色分配ERP管理菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 59
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 59);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 60
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 60);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 61
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 61);

-- 5. 为运营人员角色分配ERP管理菜单权限（如果存在，只读订单同步日志）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 59
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 59)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 61
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 61)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

-- 验证菜单是否添加成功
SELECT * FROM sys_menu WHERE menu_name IN ('ERP管理', '聚水潭配置', '订单同步日志');

-- 说明：
-- - 菜单ID 59: ERP管理（顶级目录）
-- - 菜单ID 60: 聚水潭配置（子菜单）
--   - 路径: /erp/config
--   - 组件: erp/Config
--   - 权限: admin:erp:config:manage
--   - 图标: Setting
-- - 菜单ID 61: 订单同步日志（子菜单）
--   - 路径: /erp/order-sync
--   - 组件: erp/OrderSync
--   - 权限: admin:erp:sync:list
--   - 图标: Document
