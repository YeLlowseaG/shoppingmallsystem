-- ============================================
-- 更新脚本: update-20251209-add-logistics-menu.sql
-- 更新日期: 2025-12-09
-- 更新说明: 更新现有物流配置菜单，修改组件路径和权限标识
-- 作者: ShoppingMall Team
-- ============================================

-- 1. 更新现有物流配置菜单（ID=30）信息
-- 将组件路径从 system/Logistics 改为 logistics/Index
-- 将权限标识从 admin:system:logistics 改为 admin:logistics:list
UPDATE `sys_menu` 
SET `component` = 'logistics/Index',
    `permission` = 'admin:logistics:list',
    `update_time` = NOW()
WHERE `id` = 30;

-- 2. 为超级管理员角色（role_id=1）分配物流配置菜单权限（如果还没有分配）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 1, 30, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 30
);

-- 3. 为运营人员角色（role_id=2）分配物流配置菜单权限（如果还没有分配）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`, `create_time`)
SELECT 2, 30, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 30
);

