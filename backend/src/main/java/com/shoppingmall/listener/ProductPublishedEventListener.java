package com.shoppingmall.listener;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoppingmall.event.ProductPublishedEvent;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.service.erp.JushuitanItemService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

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
    private JushuitanConfigService jushuitanConfigService;

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

            log.info("开始自动同步商品{}到聚水潭ERP", event.getProductId());

            // 调用商品同步服务
            boolean success = jushuitanItemService.uploadItem(event.getProductId());

            if (success) {
                log.info("商品{}自动同步到聚水潭ERP成功", event.getProductId());
            } else {
                log.warn("商品{}自动同步到聚水潭ERP失败", event.getProductId());
            }

        } catch (Exception e) {
            log.error("处理商品{}上架事件异常", event.getProductId(), e);
        }
    }
}
