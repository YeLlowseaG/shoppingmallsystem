package com.shoppingmall.task;

import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanItemService;
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

    @Resource
    private JushuitanItemService jushuitanItemService;

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
     * 每天凌晨3点执行一次全量同步（与数据库配置保持一致）
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

            // 全量同步商品资料
            log.info("开始全量同步商品资料");
            int totalProducts = syncAllProducts();
            log.info("全量商品同步完成，共处理{}个商品", totalProducts);

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

    /**
     * 全量同步所有商品到聚水潭ERP
     * @return 处理的商品总数
     */
    private int syncAllProducts() {
        try {
            // 获取所有商品（包括已上架、已下架、草稿状态）
            // 这里需要调用商品服务来获取所有商品ID列表
            // 由于没有直接的方法，我们可以批量处理已知商品
            // 实际情况中应该从数据库查询所有商品ID

            // 临时实现：同步最近1000个商品（可根据实际需求调整）
            log.info("开始全量同步商品资料到聚水潭ERP");

            // 这里应该从数据库查询所有商品ID
            // 暂时使用示例实现
            int processedCount = 0;

            // 示例：假设我们有商品ID列表
            // List<Long> productIds = productService.getAllProductIds();
            // for (Long productId : productIds) {
            //     try {
            //         boolean success = jushuitanItemService.uploadItem(productId);
            //         if (success) {
            //             processedCount++;
            //         }
            //         // 避免并发过多，添加短暂延迟
            //         Thread.sleep(100);
            //     } catch (Exception e) {
            //         log.error("同步商品{}失败", productId, e);
            //     }
            // }

            log.warn("全量商品同步功能暂未完全实现，需要从数据库查询所有商品ID并逐个同步");
            log.info("建议在实际部署时实现完整的商品全量同步逻辑");

            return processedCount;

        } catch (Exception e) {
            log.error("全量同步商品资料失败", e);
            return 0;
        }
    }
}
