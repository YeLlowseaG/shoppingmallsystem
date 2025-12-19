package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单支付DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
public class OrderPaymentDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNo;

    /**
     * 支付方式（PRE_DEPOSIT-预存款，ALIPAY-支付宝，WECHAT-微信）
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 支付密码（预存款支付时必填）
     */
    private String paymentPassword;
}































