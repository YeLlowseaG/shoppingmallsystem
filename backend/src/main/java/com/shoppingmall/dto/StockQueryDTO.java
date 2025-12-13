package com.shoppingmall.dto;

import lombok.Data;

/**
 * 库存查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class StockQueryDTO {

    /**
     * 商品ID（可选）
     */
    private Long productId;

    /**
     * 商品编码（可选，模糊查询）
     */
    private String productCode;

    /**
     * 商品名称（可选，模糊查询）
     */
    private String productName;

    /**
     * 是否只查询预警商品（true-只查询预警商品，false/null-查询全部）
     */
    private Boolean onlyWarning;

    /**
     * 商品状态（0-下架，1-上架）
     */
    private Integer productStatus;
}


