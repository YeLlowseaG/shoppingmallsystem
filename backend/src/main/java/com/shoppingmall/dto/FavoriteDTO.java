package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 收藏DTO
 */
@Data
public class FavoriteDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
}