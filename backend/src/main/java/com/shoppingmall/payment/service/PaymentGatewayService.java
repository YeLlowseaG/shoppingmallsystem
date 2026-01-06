package com.shoppingmall.payment.service;

import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.payment.strategy.PaymentStrategy;

/**
 * 支付网关服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface PaymentGatewayService {

    /**
     * 统一支付入口
     *
     * @param request 支付请求
     * @return 支付响应
     */
    PaymentResponseDTO pay(PaymentRequestDTO request);

    /**
     * 统一退款入口
     *
     * @param paymentMethod 支付方式
     * @param paymentNo 支付流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款流水号
     */
    String refund(String paymentMethod, String paymentNo, java.math.BigDecimal refundAmount, String refundReason);

    /**
     * 处理支付回调
     *
     * @param paymentMethod 支付方式
     * @param notifyData 回调数据
     */
    void handlePaymentNotify(String paymentMethod, Object notifyData);

    /**
     * 处理退款回调
     *
     * @param paymentMethod 支付方式
     * @param notifyData 回调数据
     */
    void handleRefundNotify(String paymentMethod, Object notifyData);

    /**
     * 获取支付策略
     *
     * @param paymentMethod 支付方式
     * @return 支付策略
     */
    PaymentStrategy getPaymentStrategy(String paymentMethod);
}


























