-- 内容管理菜单配置
-- 创建时间：2025-12-13
-- 说明：为购买咨询和商品评价功能添加后台管理菜单

USE chengren_shopping_mall;

-- 添加内容管理主菜单
INSERT IGNORE INTO sys_menu (menu_name, path, component, menu_type, parent_id, sort_order, icon, permission, status, create_time, update_time) 
VALUES 
('内容管理', '/admin/content', NULL, 0, 0, 8, 'el-icon-document', 'system:content', 1, NOW(), NOW());

-- 获取内容管理菜单ID
SET @content_menu_id = (SELECT id FROM sys_menu WHERE menu_name = '内容管理' AND parent_id = 0);

-- 添加咨询管理子菜单
INSERT IGNORE INTO sys_menu (menu_name, path, component, menu_type, parent_id, sort_order, icon, permission, status, create_time, update_time) 
VALUES 
('咨询管理', 'content/consultation', 'content/Consultation', 1, @content_menu_id, 1, 'el-icon-chat-dot-round', 'content:consultation:list', 1, NOW(), NOW());

-- 添加评价管理子菜单
INSERT IGNORE INTO sys_menu (menu_name, path, component, menu_type, parent_id, sort_order, icon, permission, status, create_time, update_time) 
VALUES 
('评价管理', 'content/review', 'content/Review', 1, @content_menu_id, 2, 'el-icon-star-on', 'content:review:list', 1, NOW(), NOW());

-- 为超级管理员角色分配菜单权限
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) 
SELECT 1, id FROM sys_menu WHERE menu_name IN ('内容管理', '咨询管理', '评价管理');

-- 验证菜单创建结果
SELECT m.id, m.menu_name, m.path, m.component, m.parent_id, m.sort_order,
       CASE WHEN rm.role_id IS NOT NULL THEN '已分配' ELSE '未分配' END AS permission_status
FROM sys_menu m 
LEFT JOIN sys_role_menu rm ON m.id = rm.menu_id AND rm.role_id = 1
WHERE m.menu_name IN ('内容管理', '咨询管理', '评价管理')
ORDER BY m.sort_order;