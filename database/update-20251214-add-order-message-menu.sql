-- ============================================
-- 更新脚本: update-20251214-add-order-message-menu.sql
-- 更新日期: 2025-12-14
-- 更新说明: 添加订单问题管理菜单和权限（在订单管理下）
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 1. 添加"订单问题"子菜单（放在订单管理下，parent_id=3）
-- 菜单ID使用54（在评论管理53之后，避免冲突）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`, `create_time`, `update_time`)
VALUES (54, 3, '订单问题', 1, 'message', 'order/OrderMessage', 'ChatLineRound', 'admin:order:message:list', 2, 1, 0, NOW(), NOW())
ON DUPLICATE KEY UPDATE 
  `menu_name` = VALUES(`menu_name`),
  `path` = VALUES(`path`),
  `component` = VALUES(`component`),
  `icon` = VALUES(`icon`),
  `permission` = VALUES(`permission`),
  `sort_order` = VALUES(`sort_order`);

-- 2. 为超级管理员角色分配订单问题菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 54
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 54);

-- 3. 为运营人员角色分配订单问题菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 54
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 54)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

-- 4. 为客服人员角色分配订单问题菜单权限（如果存在，role_id=5）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, 54
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 5 AND `menu_id` = 54)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 5);

-- 说明：
-- - 菜单ID: 54
-- - 父菜单: 订单管理 (parent_id=3)
-- - 菜单路径: /admin/order/message
-- - 组件路径: order/OrderMessage
-- - 权限标识: admin:order:message:list
-- - 排序号: 2（在订单列表之后）
-- - 图标: ChatLineRound（聊天图标）















