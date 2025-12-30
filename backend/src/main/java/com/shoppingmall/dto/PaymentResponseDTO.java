package com.shoppingmall.dto;

import lombok.Data;

/**
 * 支付响应DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class PaymentResponseDTO {

    /**
     * 内部订单号
     */
    private String internalOrderNo;

    /**
     * 支付URL（用于跳转到支付页面）
     */
    private String paymentUrl;

    /**
     * 支付参数（JSON格式，用于前端调用支付接口）
     */
    private String paymentParams;

    /**
     * 二维码URL（用于扫码支付）
     */
    private String qrCodeUrl;

    /**
     * 是否模拟支付（true-模拟支付，false-真实支付）
     */
    private Boolean isMock;

    /**
     * 模拟支付的外部交易号（仅模拟支付时使用）
     */
    private String mockExternalTradeNo;
}


























































