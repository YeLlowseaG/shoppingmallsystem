package com.shoppingmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 订单问题处理DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class OrderMessageHandleDTO {

    /**
     * 订单问题ID
     */
    @NotNull(message = "订单问题ID不能为空")
    private Long id;

    /**
     * 处理状态：1-处理中，2-已处理，3-已关闭
     */
    @NotNull(message = "处理状态不能为空")
    private Integer status;

    /**
     * 处理备注
     */
    private String handleRemark;
}





























