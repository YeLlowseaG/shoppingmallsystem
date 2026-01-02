-- 添加商品同步日志菜单
INSERT INTO `sys_menu` (`parent_id`, `menu_name`, `menu_type`, `path`, `component`, `icon`, `permission`, `sort_order`, `status`, `deleted`)
VALUES
(59, '商品同步日志', 1, 'product-sync', 'erp/ProductSync', 'Document', 'erp:product:sync:view', 3, 1, 0);
