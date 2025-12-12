package com.shoppingmall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付请求DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class PaymentRequestDTO {

    /**
     * 内部订单号（商户订单号）
     */
    @NotBlank(message = "订单号不能为空")
    private String internalOrderNo;

    /**
     * 支付金额
     */
    @NotNull(message = "支付金额不能为空")
    @DecimalMin(value = "0.01", message = "支付金额不能小于0.01元")
    private BigDecimal amount;

    /**
     * 支付方式（wechat-微信支付，alipay-支付宝）
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 支付币别（如：CNY）
     */
    @NotBlank(message = "支付币别不能为空")
    private String currency;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 回调地址
     */
    private String notifyUrl;
}



