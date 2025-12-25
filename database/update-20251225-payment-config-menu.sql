-- ============================================
-- 支付配置菜单和权限初始化脚本
-- 创建时间：2025-12-25
-- 说明：确保支付配置菜单和权限正确配置
-- ============================================

USE chengren_shopping_mall;

-- 1. 确保支付配置菜单存在（菜单ID 29）
INSERT INTO `sys_menu` (
    `id`, 
    `parent_id`, 
    `menu_name`, 
    `menu_type`, 
    `path`, 
    `component`, 
    `icon`, 
    `permission`, 
    `sort_order`, 
    `status`, 
    `deleted`, 
    `create_time`, 
    `update_time`
) VALUES (
    '29', 
    '8', 
    '支付配置', 
    '1', 
    'payment', 
    'system/Payment', 
    'CreditCard', 
    'admin:system:payment', 
    '2', 
    '1', 
    '0', 
    NOW(), 
    NOW()
) ON DUPLICATE KEY UPDATE 
    `parent_id` = VALUES(`parent_id`),
    `menu_name` = VALUES(`menu_name`),
    `menu_type` = VALUES(`menu_type`),
    `path` = VALUES(`path`),
    `component` = VALUES(`component`),
    `icon` = VALUES(`icon`),
    `permission` = VALUES(`permission`),
    `sort_order` = VALUES(`sort_order`),
    `status` = VALUES(`status`),
    `update_time` = NOW();

-- 2. 为超级管理员角色分配支付配置权限（角色ID 1）
-- 检查是否已存在该权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 29
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` 
    WHERE `role_id` = 1 AND `menu_id` = 29
);

-- 3. 为运营人员角色分配支付配置权限（假设角色ID 2，如果不存在则跳过）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 29
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` 
    WHERE `role_id` = 2 AND `menu_id` = 29
);

-- 4. 为客服人员角色分配支付配置权限（假设角色ID 3，如果不存在则跳过）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 3, 29
WHERE EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 3)
  AND NOT EXISTS (
    SELECT 1 FROM `sys_role_menu` 
    WHERE `role_id` = 3 AND `menu_id` = 29
);

-- 查询结果验证
SELECT 
    m.id,
    m.menu_name,
    m.path,
    m.component,
    m.permission,
    m.status,
    COUNT(DISTINCT rm.role_id) as role_count
FROM `sys_menu` m
LEFT JOIN `sys_role_menu` rm ON m.id = rm.menu_id
WHERE m.id = 29
GROUP BY m.id, m.menu_name, m.path, m.component, m.permission, m.status;

