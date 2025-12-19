package com.shoppingmall.vo;

import lombok.Data;

/**
 * 库存统计VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class StockStatisticsVO {

    /**
     * 商品总数
     */
    private Long totalProducts;

    /**
     * 总库存数量
     */
    private Long totalStock;

    /**
     * 可用库存总数
     */
    private Long totalAvailableStock;

    /**
     * 锁定库存总数
     */
    private Long totalLockedStock;

    /**
     * 预警商品数量
     */
    private Long warningProductCount;

    /**
     * 缺货商品数量（可用库存为0）
     */
    private Long outOfStockCount;
}
































