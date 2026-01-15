package com.shoppingmall.vo;

import lombok.Data;

/**
 * ERP同步结果VO
 *
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
@Data
public class SyncResult {

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 是否整体成功
     */
    private boolean success;

    /**
     * 商品资料同步是否成功
     */
    private Boolean itemSyncSuccess;

    /**
     * 库存同步是否成功
     */
    private Boolean inventorySyncSuccess;

    /**
     * 失败的步骤（ITEM_SYNC-商品资料同步，INVENTORY_SYNC-库存同步）
     */
    private String step;

    /**
     * 结果消息
     */
    private String message;

    /**
     * 库存同步重试次数
     */
    private Integer inventoryRetryCount;

    /**
     * 错误详情
     */
    private String errorDetail;
}

