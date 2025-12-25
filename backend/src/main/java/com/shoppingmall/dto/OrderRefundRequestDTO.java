package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 订单退款申请DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-19
 */
@Data
public class OrderRefundRequestDTO {

    /**
     * 订单号
     */
    @NotNull(message = "订单号不能为空")
    private String orderNo;

    /**
     * 退款原因
     */
    @NotNull(message = "退款原因不能为空")
    private String refundReason;

    /**
     * 退款商品明细列表
     */
    @NotEmpty(message = "请选择要退款的商品")
    private List<RefundItemDTO> refundItems;

    /**
     * 退款商品明细DTO
     */
    @Data
    public static class RefundItemDTO {
        /**
         * 订单商品ID
         */
        @NotNull(message = "订单商品ID不能为空")
        private Long orderItemId;

        /**
         * 退款数量
         */
        @NotNull(message = "退款数量不能为空")
        private Integer refundQuantity;
    }
}








