-- 添加缺货登记菜单到商品管理模块
-- 执行日期: 2025-12-15

-- 插入"缺货登记"菜单
INSERT INTO sys_menu (id, menu_name, parent_id, path, component, menu_type, sort_order, icon, permission, status, create_time, update_time)
VALUES (
    56,
    '缺货登记',
    2,
    'stock-notification',
    'product/StockNotification',
    1,
    4,
    'bell',
    'admin:product:stock',
    1,
    NOW(),
    NOW()
);

-- 给超级管理员角色分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
VALUES (1, 56, NOW());

-- 给商品管理员角色分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
VALUES (2, 56, NOW());

-- 给运营人员角色分配该菜单权限
INSERT INTO sys_role_menu (role_id, menu_id, create_time)
VALUES (3, 56, NOW());
