package com.shoppingmall.service.erp;

import com.shoppingmall.vo.SyncResult;

/**
 * 聚水潭库存服务接口
 * 
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
public interface JushuitanInventoryService {
    
    /**
     * 同步单个商品的库存到聚水潭
     * 
     * @param productId 商品ID
     * @return 是否同步成功
     */
    boolean syncInventory(Long productId);
    
    /**
     * 批量同步商品库存到聚水潭
     * 
     * @param productIds 商品ID列表
     * @return 成功同步的数量
     */
    int syncInventories(java.util.List<Long> productIds);
    
    /**
     * 同步单个SKU的库存到聚水潭
     * 
     * @param skuId SKU ID
     * @return 是否同步成功
     */
    boolean syncSkuInventory(Long skuId);
    
    /**
     * 批量同步SKU库存到聚水潭
     * 
     * @param skuIds SKU ID列表
     * @return 成功同步的数量
     */
    int syncSkuInventories(java.util.List<Long> skuIds);
    
    /**
     * 同步商品资料和库存到ERP（完整流程，带重试机制）
     * 先同步商品资料，成功后再同步库存，库存同步失败时自动重试3次
     * 
     * @param productId 商品ID
     * @return 同步结果
     */
    SyncResult syncProductAndInventory(Long productId);
}