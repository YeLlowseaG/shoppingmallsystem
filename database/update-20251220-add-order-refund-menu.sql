USE chengren_shopping_mall;

-- ============================================
-- 添加订单退款记录菜单
-- ============================================
-- 说明：在订单管理菜单下添加"订单退款记录"子菜单
-- 菜单ID: 58
-- 父菜单ID: 3 (订单管理)
-- ============================================

-- 查询该父菜单下的最大排序值
SET @max_sort_order = (SELECT IFNULL(MAX(sort_order), 0) FROM sys_menu WHERE parent_id = 3);

-- 插入订单退款记录菜单
INSERT INTO `sys_menu` (
  `id`,
  `menu_name`,
  `parent_id`,
  `menu_type`,
  `path`,
  `component`,
  `permission`,
  `icon`,
  `sort_order`,
  `status`,
  `deleted`,
  `create_time`,
  `update_time`
) VALUES (
  58,
  '订单退款记录',
  3, -- 订单管理菜单ID
  1, -- 菜单类型：1-菜单，2-按钮
  'order/RefundList',
  'order/RefundList',
  'admin:order:refund:list',
  'Money',
  @max_sort_order + 1,
  1, -- 状态：1-启用，0-禁用
  0, -- 逻辑删除：0-未删除
  NOW(),
  NOW()
) ON DUPLICATE KEY UPDATE
  menu_name = '订单退款记录',
  path = 'order/RefundList',
  component = 'order/RefundList',
  permission = 'admin:order:refund:list',
  update_time = NOW();

-- 2. 为超级管理员角色分配订单退款记录菜单权限
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 58
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 1 AND `menu_id` = 58);

-- 3. 为运营人员角色分配订单退款记录菜单权限（如果存在）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 58
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 58)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

-- 4. 为客服人员角色分配订单退款记录菜单权限（如果存在，role_id=5）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 5, 58
WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 5 AND `menu_id` = 58)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 5);

-- 验证菜单是否添加成功
SELECT * FROM sys_menu WHERE menu_name = '订单退款记录';

-- 说明：
-- - 菜单ID: 58
-- - 父菜单: 订单管理 (parent_id=3)
-- - 菜单路径: /admin/order/RefundList
-- - 组件路径: order/RefundList
-- - 权限标识: admin:order:refund:list
-- - 图标: Money（金钱图标）
