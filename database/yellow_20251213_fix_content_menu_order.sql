-- 修复内容管理菜单重复和排序冲突
-- 创建时间：2025-12-13
-- 说明：删除重复的内容管理菜单，重新排序子菜单

USE chengren_shopping_mall;

-- 删除重复的内容管理菜单及其权限关联
DELETE FROM sys_role_menu WHERE menu_id = 47;
DELETE FROM sys_menu WHERE id = 47 AND menu_name = '内容管理';

-- 重新调整内容管理下的菜单排序，避免冲突
UPDATE sys_menu SET sort_order = 1 WHERE id = 41 AND menu_name = '帮助中心';
UPDATE sys_menu SET sort_order = 2 WHERE id = 42 AND menu_name = '公告管理';  
UPDATE sys_menu SET sort_order = 3 WHERE id = 50 AND menu_name = '咨询管理';
UPDATE sys_menu SET sort_order = 4 WHERE id = 51 AND menu_name = '评价管理';

-- 验证修复结果
SELECT '=== 内容管理菜单检查 ===' as info;
SELECT id, parent_id, menu_name, path FROM sys_menu WHERE menu_name = '内容管理';

SELECT '=== 内容管理子菜单排序 ===' as info;
SELECT id, menu_name, path, sort_order, parent_id 
FROM sys_menu 
WHERE parent_id = (SELECT id FROM sys_menu WHERE menu_name = '内容管理' AND parent_id = 0)
ORDER BY sort_order;