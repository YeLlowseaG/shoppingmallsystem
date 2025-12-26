package com.shoppingmall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款请求DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class RefundRequestDTO {
    /**
     * 支付记录ID（支付记录退款时使用）
     */
    private Long paymentRecordId;

    /**
     * 预存款明细ID（预存款充值退款时使用）
     */
    private Long depositDetailId;

    /**
     * 退款金额
     */
    @NotNull(message = "退款金额不能为空")
    @DecimalMin(value = "0.01", message = "退款金额必须大于0")
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    @NotNull(message = "退款原因不能为空")
    private String refundReason;
}




















