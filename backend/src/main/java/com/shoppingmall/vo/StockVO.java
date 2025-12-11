package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 库存VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class StockVO {

    /**
     * 库存ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品主图
     */
    private String mainImage;

    /**
     * 可用库存
     */
    private Integer availableStock;

    /**
     * 锁定库存（下单锁定）
     */
    private Integer lockedStock;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 预警阈值
     */
    private Integer warningThreshold;

    /**
     * 是否预警（可用库存 <= 预警阈值）
     */
    private Boolean isWarning;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

