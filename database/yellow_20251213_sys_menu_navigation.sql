-- sys_menu导航菜单管理配置
-- 创建时间：2025-12-13
-- 说明：在后台管理系统中添加导航菜单管理功能

USE chengren_shopping_mall;

-- 添加导航菜单管理菜单项（位于系统设置下）
INSERT IGNORE INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, icon, permission, sort_order, status, deleted, create_time, update_time) 
VALUES 
(48, 8, '导航菜单', 1, 'navigation', 'system/NavigationMenu', 'Menu', 'system:navigation:list', 10, 1, 0, NOW(), NOW());

-- 为超级管理员角色分配导航菜单管理权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) 
VALUES (1, 48);

-- 验证菜单创建结果
SELECT '=== 导航菜单管理配置检查 ===' as info;
SELECT m.id, m.parent_id, m.menu_name, m.path, m.component, m.permission, m.sort_order,
       CASE WHEN rm.role_id IS NOT NULL THEN '已分配' ELSE '未分配' END AS permission_status
FROM sys_menu m 
LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id AND rm.role_id = 1
WHERE m.id = 48 OR m.menu_name = '导航菜单';

SELECT '=== 系统设置子菜单列表 ===' as info;
SELECT id, menu_name, path, sort_order 
FROM sys_menu 
WHERE parent_id = 8 
ORDER BY sort_order;