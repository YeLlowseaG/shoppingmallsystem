package com.shoppingmall.dto;

import jakarta.validation.constraints.Min;
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
     * 商品ID（通过商品ID添加时使用）
     */
    private Long productId;

    /**
     * 商品编码（通过货号添加时使用）
     */
    private String productCode;

    /**
     * 数量
     */
    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量必须大于0")
    private Integer quantity;
}












