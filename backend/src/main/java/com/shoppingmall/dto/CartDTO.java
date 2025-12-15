package com.shoppingmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 购物车DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class CartDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * SKU ID（可为空，表示无规格或未选择）
     */
    private Long skuId;

    /**
     * 规格组合（JSON字符串，如 {"颜色":"红色","尺寸":"L"}）
     */
    private String specCombination;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    private Integer quantity;
}
