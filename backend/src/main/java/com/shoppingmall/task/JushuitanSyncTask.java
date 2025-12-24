package com.shoppingmall.task;

import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * 聚水潭ERP同步定时任务
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Component
public class JushuitanSyncTask {

    @Resource
    private JushuitanConfigService jushuitanConfigService;

    @Resource
    private JushuitanLogisticsService jushuitanLogisticsService;

    /**
     * 定时拉取物流信息
     * 默认每30分钟执行一次
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void pullLogisticsTask() {
        try {
            // 检查ERP配置是否启用
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过物流拉取定时任务");
                return;
            }

            // 检查是否开启自动拉取物流
            if (config.getAutoPullLogistics() == null || config.getAutoPullLogistics() != 1) {
                log.debug("聚水潭未开启自动拉取物流，跳过定时任务");
                return;
            }

            log.info("开始执行聚水潭物流拉取定时任务");

            // 拉取所有待发货订单的物流信息
            int successCount = jushuitanLogisticsService.pullPendingLogistics();

            log.info("聚水潭物流拉取定时任务执行完成，成功拉取{}个订单的物流信息", successCount);

        } catch (Exception e) {
            log.error("聚水潭物流拉取定时任务执行失败", e);
        }
    }

    /**
     * 每天凌晨3点执行一次全量同步
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void fullSyncTask() {
        try {
            // 检查ERP配置是否启用
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过全量同步定时任务");
                return;
            }

            log.info("开始执行聚水潭全量同步定时任务");

            // 拉取物流信息
            if (config.getAutoPullLogistics() != null && config.getAutoPullLogistics() == 1) {
                int successCount = jushuitanLogisticsService.pullPendingLogistics();
                log.info("全量物流同步完成，成功{}个订单", successCount);
            }

            log.info("聚水潭全量同步定时任务执行完成");

        } catch (Exception e) {
            log.error("聚水潭全量同步定时任务执行失败", e);
        }
    }
}
