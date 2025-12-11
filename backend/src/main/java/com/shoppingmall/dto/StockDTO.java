package com.shoppingmall.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 库存DTO（用于库存调整）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class StockDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 调整数量（正数为增加，负数为减少）
     */
    @NotNull(message = "调整数量不能为空")
    private Integer adjustQuantity;

    /**
     * 调整原因
     */
    private String reason;

    /**
     * 预警阈值（可选，用于更新预警阈值）
     */
    @Min(value = 0, message = "预警阈值不能为负数")
    private Integer warningThreshold;
}


