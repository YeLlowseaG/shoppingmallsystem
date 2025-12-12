-- 简化库存管理功能
-- 创建时间: 2025-12-11
-- 更新说明: 简化库存管理功能，只保留库存列表页面（包含查询和调整功能），禁用库存预警、库存调整、库存统计菜单

-- 禁用库存预警菜单（id=16）
UPDATE `sys_menu` 
SET `status` = '0', `update_time` = NOW()
WHERE `id` = 16;

-- 禁用库存调整菜单（id=17）
UPDATE `sys_menu` 
SET `status` = '0', `update_time` = NOW()
WHERE `id` = 17;

-- 禁用库存统计菜单（id=18）
UPDATE `sys_menu` 
SET `status` = '0', `update_time` = NOW()
WHERE `id` = 18;

-- 说明：
-- 1. 库存列表（id=15）保留，功能已整合查询和调整功能
-- 2. 库存预警、库存调整、库存统计菜单已禁用（status=0），不再显示在管理后台
-- 3. 如需恢复，可将 status 字段改为 '1'

