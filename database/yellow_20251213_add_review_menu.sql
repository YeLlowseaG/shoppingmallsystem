-- 添加评论管理菜单
-- 创建时间: 2025-12-13

USE `chengren_shopping_mall`;

-- 1. 添加"评论管理"菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (53, 0, '评论管理', 1, 'review', 'review/Index', 'ChatDotRound', 'admin:review:list', 8, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 2. 为超级管理员角色分配评论管理菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 53
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 53);

-- 3. 为运营人员角色分配评论管理菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 53
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 53)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);