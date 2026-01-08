package com.shoppingmall.payment.strategy;

import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;

/**
 * 支付策略接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface PaymentStrategy {

    /**
     * 创建支付订单
     *
     * @param request 支付请求信息
     * @return 支付响应信息（包含支付URL或支付参数）
     */
    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    /**
     * 验证支付回调数据
     *
     * @param callbackData 回调数据
     * @return 是否验证通过
     */
    boolean verifyCallback(Object callbackData);

    /**
     * 查询支付状态
     *
     * @param paymentNo 支付流水号
     * @return 支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败）
     */
    Integer queryPaymentStatus(String paymentNo);

    /**
     * 申请退款
     *
     * @param paymentNo 原支付流水号
     * @param refundAmount 退款金额
     * @param refundReason 退款原因
     * @return 退款流水号
     */
    String refund(String paymentNo, java.math.BigDecimal refundAmount, String refundReason);

    /**
     * 查询退款状态
     *
     * @param refundNo 退款流水号
     * @return 退款状态（0-退款中，1-退款成功，2-退款失败）
     */
    Integer queryRefundStatus(String refundNo);

    /**
     * 验证退款回调
     *
     * @param callbackData 回调数据
     * @return 是否验证通过
     */
    boolean verifyRefundCallback(Object callbackData);
}






























