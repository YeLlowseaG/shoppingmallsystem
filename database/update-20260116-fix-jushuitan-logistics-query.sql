-- 更新聚水潭物流拉取任务的执行频率为每10分钟
-- 同时更新描述，说明查询范围包括已付款未发货和已发货的订单
UPDATE `scheduled_task` 
SET `cron_expression` = '0 */10 * * * ?',
    `description` = '每10分钟执行一次，拉取聚水潭ERP中待发货和已发货订单的物流信息（需配置启用）',
    `update_time` = NOW()
WHERE `task_name` = '聚水潭物流拉取' AND `task_group` = 'ERP同步';

