package com.shoppingmall.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 预存款充值DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class DepositRechargeDTO {

    /**
     * 充值金额
     */
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额不能小于0.01元")
    private BigDecimal amount;

    /**
     * 支付币别（如：CNY）
     */
    @NotBlank(message = "支付币别不能为空")
    private String currency;

    /**
     * 支付方式（wechat-微信支付，alipay-支付宝）
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;
}


































































