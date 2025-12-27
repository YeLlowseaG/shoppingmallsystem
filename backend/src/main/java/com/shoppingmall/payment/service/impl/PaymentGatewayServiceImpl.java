package com.shoppingmall.payment.service.impl;

import com.shoppingmall.common.constant.PaymentMethod;
import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.service.PaymentGatewayService;
import com.shoppingmall.payment.strategy.PaymentStrategy;
import com.shoppingmall.payment.strategy.impl.AlipayPayStrategy;
import com.shoppingmall.payment.strategy.impl.WeChatPayStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 支付网关服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private final PaymentConfigService paymentConfigService;
    private final WeChatPayStrategy weChatPayStrategy;
    private final AlipayPayStrategy alipayPayStrategy;

    @Override
    public PaymentResponseDTO pay(PaymentRequestDTO request) {
        log.info("支付网关处理支付请求，订单号：{}，支付方式：{}，金额：{}",
                request.getInternalOrderNo(), request.getPaymentMethod(), request.getAmount());

        // 获取支付策略
        PaymentStrategy strategy = getPaymentStrategy(request.getPaymentMethod());
        if (strategy == null) {
            throw new PaymentException(400, "不支持的支付方式：" + request.getPaymentMethod());
        }

        // 检查支付方式是否启用
        String paymentMethod = request.getPaymentMethod().toUpperCase();
        if (!paymentConfigService.isPaymentEnabled(paymentMethod)) {
            // 支付方式未启用，自动切换到模拟支付模式（方便测试）
            log.warn("支付方式未启用：{}，自动切换到模拟支付模式，订单号：{}", paymentMethod, request.getInternalOrderNo());
            return createMockPaymentResponse(request);
        }

        // 调用策略创建支付订单
        return strategy.createPayment(request);
    }

    /**
     * 创建模拟支付响应（当支付方式未启用时使用）
     * 方便测试，无需配置第三方支付即可测试支付流程
     *
     * @param request 支付请求信息
     * @return 模拟支付响应
     */
    private PaymentResponseDTO createMockPaymentResponse(PaymentRequestDTO request) {
        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setInternalOrderNo(request.getInternalOrderNo());
        response.setIsMock(true);
        
        // 生成模拟的外部交易号
        String mockExternalTradeNo = "MOCK_" + request.getPaymentMethod().toUpperCase() + "_" 
                + System.currentTimeMillis() + "_" 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        response.setMockExternalTradeNo(mockExternalTradeNo);
        
        log.info("创建模拟支付响应，订单号：{}，支付方式：{}，模拟交易号：{}，金额：{}", 
                request.getInternalOrderNo(), request.getPaymentMethod(), mockExternalTradeNo, request.getAmount());
        
        return response;
    }

    @Override
    public String refund(String paymentMethod, String paymentNo, BigDecimal refundAmount, String refundReason) {
        log.info("支付网关处理退款请求，支付方式：{}，支付流水号：{}，退款金额：{}",
                paymentMethod, paymentNo, refundAmount);

        // 获取支付策略
        PaymentStrategy strategy = getPaymentStrategy(paymentMethod);
        if (strategy == null) {
            throw new PaymentException(400, "不支持的支付方式：" + paymentMethod);
        }

        // 检查支付方式是否启用
        String method = paymentMethod.toUpperCase();
        if (!paymentConfigService.isPaymentEnabled(method)) {
            // 支付方式未启用，自动切换到模拟退款模式（方便测试）
            log.warn("支付方式未启用：{}，自动切换到模拟退款模式，支付流水号：{}，退款金额：{}", 
                    method, paymentNo, refundAmount);
            return createMockRefundNo(paymentMethod, paymentNo);
        }

        // 调用策略申请退款
        return strategy.refund(paymentNo, refundAmount, refundReason);
    }

    /**
     * 创建模拟退款流水号（当支付方式未启用时使用）
     * 方便测试，无需配置第三方支付即可测试退款流程
     *
     * @param paymentMethod 支付方式
     * @param paymentNo 支付流水号（订单号）
     * @return 模拟退款流水号
     */
    private String createMockRefundNo(String paymentMethod, String paymentNo) {
        // 生成模拟的退款流水号
        String mockRefundNo = "MOCK_REFUND_" + paymentMethod.toUpperCase() + "_" 
                + System.currentTimeMillis() + "_" 
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        log.info("创建模拟退款流水号，支付方式：{}，支付流水号：{}，模拟退款流水号：{}", 
                paymentMethod, paymentNo, mockRefundNo);
        
        return mockRefundNo;
    }

    @Override
    public void handlePaymentNotify(String paymentMethod, Object notifyData) {
        log.info("支付网关处理支付回调，支付方式：{}", paymentMethod);

        // 获取支付策略
        PaymentStrategy strategy = getPaymentStrategy(paymentMethod);
        if (strategy == null) {
            log.warn("不支持的支付方式：{}", paymentMethod);
            return;
        }

        // 验证回调数据
        boolean verified = strategy.verifyCallback(notifyData);
        if (!verified) {
            log.warn("支付回调验证失败，支付方式：{}", paymentMethod);
            throw new PaymentException(400, "支付回调验证失败");
        }

        // TODO: 处理支付回调业务逻辑（更新订单状态、支付记录等）
        // 这部分逻辑应该在Controller中处理，这里只负责验证
        log.info("支付回调验证成功，支付方式：{}", paymentMethod);
    }

    @Override
    public void handleRefundNotify(String paymentMethod, Object notifyData) {
        log.info("支付网关处理退款回调，支付方式：{}", paymentMethod);

        // 获取支付策略
        PaymentStrategy strategy = getPaymentStrategy(paymentMethod);
        if (strategy == null) {
            log.warn("不支持的支付方式：{}", paymentMethod);
            return;
        }

        // 验证回调数据
        boolean verified = strategy.verifyRefundCallback(notifyData);
        if (!verified) {
            log.warn("退款回调验证失败，支付方式：{}", paymentMethod);
            throw new PaymentException(400, "退款回调验证失败");
        }

        // TODO: 处理退款回调业务逻辑（更新退款状态等）
        // 这部分逻辑应该在Controller中处理，这里只负责验证
        log.info("退款回调验证成功，支付方式：{}", paymentMethod);
    }

    @Override
    public PaymentStrategy getPaymentStrategy(String paymentMethod) {
        if (paymentMethod == null) {
            return null;
        }

        String method = paymentMethod.toUpperCase();
        switch (method) {
            case PaymentMethod.WECHAT:
                return weChatPayStrategy;
            case PaymentMethod.ALIPAY:
                return alipayPayStrategy;
            default:
                log.warn("不支持的支付方式：{}", paymentMethod);
                return null;
        }
    }
}

