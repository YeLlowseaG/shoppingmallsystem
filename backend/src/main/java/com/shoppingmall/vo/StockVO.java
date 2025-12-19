package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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
     * 商品状态（0-下架，1-上架）
     */
    private Integer productStatus;

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

    /**
     * 是否启用规格
     */
    private Boolean enableSpec;

    /**
     * SKU库存列表（当启用规格时有值）
     */
    private List<SkuStockVO> skuStockList;

    /**
     * SKU库存VO（内部类）
     */
    @Data
    public static class SkuStockVO {
        /**
         * SKU ID
         */
        private Long skuId;

        /**
         * SKU编码
         */
        private String skuCode;

        /**
         * 规格组合（JSON格式，如：{"颜色":"红色","尺寸":"L"}）
         */
        private String specCombination;

        /**
         * 规格组合显示文本（如：红色/L）
         */
        private String specText;

        /**
         * SKU库存
         */
        private Integer stock;

        /**
         * SKU预警库存
         */
        private Integer warningStock;

        /**
         * 是否预警
         */
        private Boolean isWarning;
    }
}

