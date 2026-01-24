package com.shoppingmall.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoppingmall.entity.ProductSyncLog;
import com.shoppingmall.event.ProductPublishedEvent;
import com.shoppingmall.mapper.ProductSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.service.erp.JushuitanInventoryService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品保存事件监听器
 * 监听商品保存事件，自动同步到聚水潭ERP
 *
 * @author ShoppingMall Team
 * @date 2025-01-14
 */
@Slf4j
@Component
public class ProductPublishedEventListener {

    @Resource
    private JushuitanItemService jushuitanItemService;

    @Resource
    private JushuitanInventoryService jushuitanInventoryService;

    @Resource
    private JushuitanConfigService jushuitanConfigService;

    @Resource
    private ProductSyncLogMapper productSyncLogMapper;

    /**
     * 监听商品保存事件，自动同步到ERP
     */
    @EventListener
    @Async
    public void handleProductPublished(ProductPublishedEvent event) {
        try {
            log.info("监听到商品保存事件: productId={}, productCode={}, productName={}",
                    event.getProductId(), event.getProductCode(), event.getProductName());

            // 检查聚水潭配置是否启用自动同步商品
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config == null) {
                log.debug("聚水潭配置未启用，跳过自动同步");
                return;
            }

            log.debug("聚水潭配置检查: autoSyncProduct={}", config.getAutoSyncProduct());

            if (config.getAutoSyncProduct() == null || config.getAutoSyncProduct() != 1) {
                log.debug("聚水潭未开启自动同步商品功能，跳过自动同步");
                return;
            }

            // 检查最近10秒内是否已经同步过该商品（避免重复同步）
            // 如果前端已经调用了同步接口，后端事件监听器就不需要再同步了
            // 注意：不仅检查成功的记录，也检查失败的记录，因为即使失败也说明已经尝试过同步
            LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(10);
            QueryWrapper<ProductSyncLog> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("product_id", event.getProductId());
            checkWrapper.in("sync_type", "UPLOAD_ITEM", "UPLOAD_SHOP_ITEM", "INVENTORY_SYNC");
            // 不限制sync_status，检查所有同步记录（成功和失败都算）
            checkWrapper.ge("create_time", thirtySecondsAgo); // 最近10秒内的记录
            checkWrapper.orderByDesc("create_time");
            checkWrapper.last("LIMIT 1");
            
            List<ProductSyncLog> recentLogs = productSyncLogMapper.selectList(checkWrapper);
            if (recentLogs != null && !recentLogs.isEmpty()) {
                ProductSyncLog recentLog = recentLogs.get(0);
                log.info("商品{}在最近10秒内已经同步过（同步类型: {}, 同步状态: {}, 同步时间: {}），跳过自动同步，避免重复", 
                        event.getProductId(), recentLog.getSyncType(), 
                        recentLog.getSyncStatus() == 1 ? "成功" : "失败",
                        recentLog.getCreateTime());
                return;
            }

            log.info("开始自动同步商品{}到聚水潭ERP（完整流程：商品资料 + 库存）", event.getProductId());

            // 使用完整同步流程：先同步商品资料，成功后再同步库存（带重试机制）
            com.shoppingmall.vo.SyncResult syncResult = jushuitanInventoryService.syncProductAndInventory(event.getProductId());

            if (syncResult.isSuccess()) {
                log.info("商品{}完整同步到聚水潭ERP成功（商品资料: {}, 库存: {}）", 
                        event.getProductId(), 
                        syncResult.getItemSyncSuccess() ? "成功" : "失败",
                        syncResult.getInventorySyncSuccess() ? "成功" : "失败");
            } else {
                log.warn("商品{}完整同步到聚水潭ERP失败: {}, 失败步骤: {}", 
                        event.getProductId(), 
                        syncResult.getMessage(),
                        syncResult.getStep());
                
                // 如果库存同步失败，记录重试次数
                if (syncResult.getInventoryRetryCount() != null && syncResult.getInventoryRetryCount() > 0) {
                    log.warn("商品{}库存同步重试{}次后仍然失败", 
                            event.getProductId(), 
                            syncResult.getInventoryRetryCount());
                }
            }

        } catch (Exception e) {
            log.error("处理商品{}上架事件异常", event.getProductId(), e);
        }
    }
}
