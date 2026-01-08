-- 添加帮助中心管理菜单
-- 创建时间: 2025-12-10

-- 1. 添加"内容管理"一级菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (40, 0, '内容管理', 0, '/admin/content', NULL, 'Document', NULL, 8, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `sort_order` = VALUES(`sort_order`);

-- 2. 添加"帮助中心"二级菜单
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (41, 40, '帮助中心', 1, '/admin/content/help', 'help/Index', 'QuestionFilled', 'admin:help:list', 1, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE `menu_name` = VALUES(`menu_name`), `component` = VALUES(`component`), `permission` = VALUES(`permission`);

-- 3. 为超级管理员角色分配菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 40
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 40);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 41
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 41);

-- 4. 为运营人员角色分配菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 40
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 40)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 41
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 41)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);







































































