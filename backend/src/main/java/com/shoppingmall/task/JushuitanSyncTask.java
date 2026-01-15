package com.shoppingmall.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.Product;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanInventoryService;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

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

    @Resource
    private JushuitanInventoryService jushuitanInventoryService;

    @Resource
    private ProductRepository productRepository;

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
     * 每天凌晨1点执行一次商品资料全量同步
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void syncProductInfoTask() {
        try {
            // 检查ERP配置是否启用
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过商品资料同步定时任务");
                return;
            }

            log.info("开始执行聚水潭商品资料全量同步定时任务");

            // 全量同步商品资料
            int totalProducts = syncAllProducts();
            log.info("聚水潭商品资料全量同步定时任务执行完成，共处理{}个商品", totalProducts);

        } catch (Exception e) {
            log.error("聚水潭商品资料全量同步定时任务执行失败", e);
        }
    }

    /**
     * 每天凌晨2点执行一次库存数据全量同步
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void syncInventoryTask() {
        try {
            // 检查ERP配置是否启用
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过库存同步定时任务");
                return;
            }

            log.info("开始执行聚水潭库存数据全量同步定时任务");

            // 全量同步库存数据
            int totalProducts = syncAllInventories();
            log.info("聚水潭库存数据全量同步定时任务执行完成，共处理{}个商品", totalProducts);

        } catch (Exception e) {
            log.error("聚水潭库存数据全量同步定时任务执行失败", e);
        }
    }

    /**
     * 每天凌晨3点执行一次全量同步（保留原方法，兼容旧配置，但建议使用新的拆分任务）
     * @deprecated 建议使用 syncProductInfoTask 和 syncInventoryTask 替代
     */
    @Deprecated
    @Scheduled(cron = "0 0 3 * * ?")
    public void fullSyncTask() {
        try {
            // 检查ERP配置是否启用
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过全量同步定时任务");
                return;
            }

            log.info("开始执行聚水潭全量同步定时任务（已废弃，建议使用拆分后的任务）");

            // 全量同步商品资料
            log.info("开始全量同步商品资料");
            int totalProducts = syncAllProducts();
            log.info("全量商品同步完成，共处理{}个商品", totalProducts);

            // 全量同步库存数据
            log.info("开始全量同步库存数据");
            int inventoryCount = syncAllInventories();
            log.info("全量库存同步完成，共处理{}个商品", inventoryCount);

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
     * 全量同步所有商品资料到聚水潭ERP（只同步上架状态的商品）
     * @return 成功处理的商品总数
     */
    private int syncAllProducts() {
        try {
            log.info("开始全量同步商品资料到聚水潭ERP（只同步上架商品）");

            // 查询所有上架状态的商品（status=1）
            // MyBatis-Plus 的 @TableLogic 会自动过滤 deleted=1 的记录
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Product::getId); // 只查询ID，提高性能
            wrapper.eq(Product::getStatus, 1); // 只查询上架状态的商品（1=上架，0=下架，2=草稿）
            wrapper.orderByDesc(Product::getCreateTime); // 按创建时间倒序，优先同步新商品

            List<Product> products = productRepository.selectList(wrapper);
            int totalCount = products.size();

            if (totalCount == 0) {
                log.info("未查询到需要同步的上架商品");
                return 0;
            }

            log.info("共查询到{}个上架商品，开始逐个同步商品资料", totalCount);

            int successCount = 0;
            int failCount = 0;

            // 逐个同步商品资料
            for (Product product : products) {
                try {
                    boolean success = jushuitanItemService.uploadItem(product.getId());
                    if (success) {
                        successCount++;
                        if (successCount % 10 == 0) {
                            log.info("商品资料同步进度: {}/{}", successCount, totalCount);
                        }
                    } else {
                        failCount++;
                        log.warn("商品{}资料同步失败", product.getId());
                    }

                    // 避免并发过多，添加短暂延迟
                    Thread.sleep(100);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("商品资料同步被中断", e);
                    break;
                } catch (Exception e) {
                    failCount++;
                    log.error("同步商品{}资料失败", product.getId(), e);
                }
            }

            log.info("全量商品资料同步完成，成功: {}, 失败: {}, 总计: {}", successCount, failCount, totalCount);
            return successCount;

        } catch (Exception e) {
            log.error("全量同步商品资料失败", e);
            return 0;
        }
    }

    /**
     * 全量同步所有商品库存到聚水潭ERP（只同步上架状态的商品）
     * @return 成功处理的商品总数
     */
    private int syncAllInventories() {
        try {
            log.info("开始全量同步库存数据到聚水潭ERP（只同步上架商品）");

            // 查询所有上架状态的商品（status=1）
            LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(Product::getId); // 只查询ID，提高性能
            wrapper.eq(Product::getStatus, 1); // 只查询上架状态的商品（1=上架，0=下架，2=草稿）
            wrapper.orderByDesc(Product::getCreateTime); // 按创建时间倒序

            List<Product> products = productRepository.selectList(wrapper);
            int totalCount = products.size();

            if (totalCount == 0) {
                log.info("未查询到需要同步库存的上架商品");
                return 0;
            }

            log.info("共查询到{}个上架商品，开始逐个同步库存数据", totalCount);

            // 提取商品ID列表
            List<Long> productIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            // 批量同步库存（内部会逐个处理）
            int successCount = jushuitanInventoryService.syncInventories(productIds);

            log.info("全量库存数据同步完成，成功: {}, 总计: {}", successCount, totalCount);
            return successCount;

        } catch (Exception e) {
            log.error("全量同步库存数据失败", e);
            return 0;
        }
    }
}
