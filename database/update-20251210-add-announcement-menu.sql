-- 添加公告管理菜单
-- 创建时间: 2025-12-10
-- 更新说明: 在内容管理下添加公告管理菜单

-- 添加公告管理菜单（在内容管理下）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (42, 40, '公告管理', 1, '/admin/content/announcement', 'announcement/Index', 'Document', 'admin:announcement:list', 2, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 为超级管理员角色分配菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 42
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 42);

-- 为运营人员角色分配菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 42
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 42)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

