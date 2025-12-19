package com.shoppingmall.service.payment;

import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;

/**
 * 支付服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface PaymentService {

    /**
     * 发起支付（创建支付订单）
     *
     * @param request 支付请求信息
     * @return 支付响应信息（包含支付URL或支付参数）
     */
    PaymentResponseDTO createPayment(PaymentRequestDTO request);

    /**
     * 验证支付回调数据
     *
     * @param paymentMethod 支付方式（wechat/alipay）
     * @param callbackData 回调数据
     * @return 是否验证通过
     */
    boolean verifyCallback(String paymentMethod, Object callbackData);
}

































