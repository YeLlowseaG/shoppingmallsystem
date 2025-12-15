package com.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 商品评价DTO
 */
@Data
public class ProductReviewDTO {

    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 评分（已废弃，保留字段兼容性，不进行校验）
     */
    private Integer rating;

    /**
     * 评价内容
     */
    @Size(max = 1000, message = "评价内容不能超过1000个字符")
    private String reviewContent;

    /**
     * 评价图片URL列表
     */
    private List<String> reviewImages;
}