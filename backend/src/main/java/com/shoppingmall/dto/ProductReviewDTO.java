package com.shoppingmall.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
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
     * 评分：1-5星
     */
    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低为1星")
    @Max(value = 5, message = "评分最高为5星")
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