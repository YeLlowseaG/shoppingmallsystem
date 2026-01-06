package com.shoppingmall.service.payment.impl;

import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.service.payment.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 支付服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    /**
     * 是否启用模拟支付（true-模拟支付，false-真实支付）
     * 可以通过配置文件配置，默认true（方便测试）
     */
    @Value("${payment.mock.enabled:true}")
    private Boolean mockEnabled;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setInternalOrderNo(request.getInternalOrderNo());

        if (Boolean.TRUE.equals(mockEnabled)) {
            // 模拟支付模式
            log.info("使用模拟支付模式，内部订单号：{}，支付方式：{}，金额：{}", 
                    request.getInternalOrderNo(), request.getPaymentMethod(), request.getAmount());

            // 生成模拟的外部交易号
            String mockExternalTradeNo = generateMockExternalTradeNo(request.getPaymentMethod());
            response.setMockExternalTradeNo(mockExternalTradeNo);
            response.setIsMock(true);

            // 模拟支付URL（实际应该跳转到支付页面）
            if ("wechat".equals(request.getPaymentMethod())) {
                response.setPaymentUrl("/payment/mock/wechat?orderNo=" + request.getInternalOrderNo());
                response.setQrCodeUrl("/payment/mock/qrcode/wechat?orderNo=" + request.getInternalOrderNo());
            } else if ("alipay".equals(request.getPaymentMethod())) {
                response.setPaymentUrl("/payment/mock/alipay?orderNo=" + request.getInternalOrderNo());
                response.setQrCodeUrl("/payment/mock/qrcode/alipay?orderNo=" + request.getInternalOrderNo());
            }

            log.info("模拟支付订单创建成功，内部订单号：{}，模拟外部交易号：{}", 
                    request.getInternalOrderNo(), mockExternalTradeNo);
        } else {
            // 真实支付模式
            log.info("使用真实支付模式，内部订单号：{}，支付方式：{}，金额：{}", 
                    request.getInternalOrderNo(), request.getPaymentMethod(), request.getAmount());

            response.setIsMock(false);

            // TODO: 调用真实的微信/支付宝支付接口
            // 微信支付示例：
            // if ("wechat".equals(request.getPaymentMethod())) {
            //     WeChatPayResult result = weChatPayService.createOrder(request);
            //     response.setPaymentUrl(result.getPaymentUrl());
            //     response.setQrCodeUrl(result.getQrCodeUrl());
            //     response.setPaymentParams(result.getPaymentParams());
            // }
            // 
            // 支付宝支付示例：
            // if ("alipay".equals(request.getPaymentMethod())) {
            //     AlipayResult result = alipayService.createOrder(request);
            //     response.setPaymentUrl(result.getPaymentUrl());
            //     response.setQrCodeUrl(result.getQrCodeUrl());
            //     response.setPaymentParams(result.getPaymentParams());
            // }

            // 暂时抛出异常，提示需要实现真实支付接口
            throw new UnsupportedOperationException("真实支付接口尚未实现，请先启用模拟支付模式（payment.mock.enabled=true）");
        }

        return response;
    }

    @Override
    public boolean verifyCallback(String paymentMethod, Object callbackData) {
        if (Boolean.TRUE.equals(mockEnabled)) {
            // 模拟支付模式下，直接返回true
            log.info("模拟支付模式，跳过回调验证，支付方式：{}", paymentMethod);
            return true;
        }

        // TODO: 实现真实的回调验证逻辑
        // 微信支付验证示例：
        // if ("wechat".equals(paymentMethod)) {
        //     return weChatPayService.verifyCallback(callbackData);
        // }
        //
        // 支付宝支付验证示例：
        // if ("alipay".equals(paymentMethod)) {
        //     return alipayService.verifyCallback(callbackData);
        // }

        log.warn("真实支付回调验证尚未实现，支付方式：{}", paymentMethod);
        return false;
    }

    /**
     * 生成模拟的外部交易号
     */
    private String generateMockExternalTradeNo(String paymentMethod) {
        String prefix = "wechat".equals(paymentMethod) ? "WX" : "ALI";
        return prefix + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}



































































