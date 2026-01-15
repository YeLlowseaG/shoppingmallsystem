-- ============================================
-- 更新脚本: update-20260115-split-jushuitan-sync-tasks.sql
-- 更新日期: 2026-01-15
-- 更新说明: 将聚水潭全量同步任务拆分为商品资料同步和库存数据同步两个独立任务
-- 作者: ShoppingMall Team
-- ============================================

USE `chengren_shopping_mall`;

-- 1. 添加聚水潭商品资料全量同步任务（凌晨1点执行）
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('聚水潭商品资料同步(已上架的商品)', 'ERP同步', '0 0 1 * * ?', 'jushuitanSyncTask', 'syncProductInfoTask', 1, '每天凌晨1点执行一次，全量同步上架商品资料到聚水潭ERP（只同步上架状态商品，需配置启用）')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`),
  `update_time` = NOW();

-- 2. 添加聚水潭库存数据全量同步任务（凌晨2点执行）
INSERT INTO `scheduled_task` (`task_name`, `task_group`, `cron_expression`, `bean_name`, `method_name`, `status`, `description`)
VALUES ('聚水潭库存数据同步(已上架的商品)', 'ERP同步', '0 0 2 * * ?', 'jushuitanSyncTask', 'syncInventoryTask', 1, '每天凌晨2点执行一次，全量同步上架商品库存数据到聚水潭ERP（只同步上架状态商品，需配置启用）')
ON DUPLICATE KEY UPDATE 
  `cron_expression` = VALUES(`cron_expression`),
  `bean_name` = VALUES(`bean_name`),
  `method_name` = VALUES(`method_name`),
  `description` = VALUES(`description`),
  `update_time` = NOW();

-- 3. 删除原"聚水潭全量同步"任务（保留兼容性，但建议使用拆分后的任务）
delete from scheduled_task   WHERE `task_name` = '聚水潭全量同步'
  AND `bean_name` = 'jushuitanSyncTask'
  AND `method_name` = 'fullSyncTask';

-- ============================================
-- 说明：
-- 1. 新增两个独立的定时任务：
--    - 聚水潭商品资料同步：每天凌晨1点执行（cron = "0 0 1 * * ?"），只同步上架状态商品
--    - 聚水潭库存数据同步：每天凌晨2点执行（cron = "0 0 2 * * ?"），只同步上架状态商品
-- 2. 原"聚水潭全量同步"任务保留但标记为废弃，建议禁用或删除
-- 3. 两个任务可以独立控制启用/禁用，更加灵活
-- 4. 只同步上架状态（status=1）的商品，下架和草稿状态的商品不会同步
-- ============================================

