-- 更新聚水潭全量同步定时任务的描述
-- 执行时间: 2025-01-14
-- 说明: 更新定时任务描述，说明现在包含商品资料同步功能

-- 更新聚水潭全量同步任务的描述
UPDATE `scheduled_task`
SET `description` = '每天凌晨3点执行一次，执行聚水潭ERP全量同步任务（物流同步+商品资料同步，需配置启用）',
    `update_time` = NOW()
WHERE `task_name` = '聚水潭全量同步'
  AND `bean_name` = 'jushuitanSyncTask'
  AND `method_name` = 'fullSyncTask';







