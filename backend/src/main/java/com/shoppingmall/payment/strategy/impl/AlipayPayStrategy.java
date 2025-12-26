package com.shoppingmall.payment.strategy.impl;

import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.strategy.PaymentStrategy;
import com.shoppingmall.payment.util.AlipayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.HashMap;

/**
 * 支付宝支付策略实现
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AlipayPayStrategy implements PaymentStrategy {

    private final PaymentConfigService paymentConfigService;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        log.info("创建支付宝支付订单，订单号：{}，金额：{}", request.getInternalOrderNo(), request.getAmount());

        // 获取支付宝配置
        AlipayConfig config = paymentConfigService.getAlipayConfig();
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            throw new PaymentException(400, "支付宝未启用");
        }

        // 根据环境获取配置
        AlipayConfig.AlipayEnvConfig envConfig = "production".equals(config.getEnv())
                ? config.getProduction()
                : config.getSandbox();

        log.debug("支付宝环境选择: env={}, appid={}", config.getEnv(), envConfig != null ? envConfig.getAppid() : "null");

        if (envConfig == null || envConfig.getAppid() == null || envConfig.getAppid().isEmpty()) {
            throw new PaymentException(400, "支付宝配置不完整，请先配置支付参数");
        }

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setInternalOrderNo(request.getInternalOrderNo());
        response.setIsMock(false);

        try {
            // 获取回调地址
            String notifyUrl = config.getNotifyUrl();
            if (notifyUrl == null || notifyUrl.isEmpty()) {
                notifyUrl = request.getNotifyUrl();
            }

            // 金额转换为字符串（支付宝使用元为单位）
            String amountStr = request.getAmount().toString();

            // 创建页面支付表单（用于跳转支付）
            String paymentForm = AlipayUtil.createPagePayment(
                    envConfig,
                    request.getInternalOrderNo(),
                    amountStr,
                    request.getDescription() != null ? request.getDescription() : "商品支付",
                    notifyUrl);

            response.setPaymentParams(paymentForm); // 支付表单HTML
            response.setPaymentUrl(null); // 页面支付不需要单独的支付URL，表单会自动提交跳转

            log.info("支付宝支付订单创建成功，订单号：{}", request.getInternalOrderNo());
        } catch (Exception e) {
            log.error("创建支付宝支付订单失败，订单号：{}", request.getInternalOrderNo(), e);
            throw new PaymentException(500, "创建支付宝支付订单失败：" + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public boolean verifyCallback(Object callbackData) {
        log.info("验证支付宝回调数据");

        if (callbackData == null) {
            log.warn("支付宝回调数据为空");
            return false;
        }

        try {
            // 获取配置
            AlipayConfig config = paymentConfigService.getAlipayConfig();
            if (config == null) {
                log.warn("支付宝配置不存在");
                return false;
            }

            AlipayConfig.AlipayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null || envConfig.getPublicKey() == null || envConfig.getPublicKey().isEmpty()) {
                log.warn("支付宝配置不完整");
                return false;
            }

            // 验证签名
            if (callbackData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) callbackData;

                // 转换为String类型的Map用于签名验证
                Map<String, String> params = new HashMap<>();
                for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                    if (entry.getValue() != null) {
                        params.put(entry.getKey(), entry.getValue().toString());
                    }
                }

                return AlipayUtil.verifySign(params, envConfig.getPublicKey());
            }
            log.warn("支付宝回调数据格式不正确");
            return false;

        } catch (Exception e) {
            log.error("验证支付宝回调数据失败", e);
            return false;
        }
    }

    @Override
    public Integer queryPaymentStatus(String paymentNo) {
        log.info("查询支付宝支付状态，支付流水号：{}", paymentNo);

        try {
            // 获取配置
            AlipayConfig config = paymentConfigService.getAlipayConfig();
            if (config == null) {
                return PaymentStatus.FAILED;
            }

            AlipayConfig.AlipayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null) {
                return PaymentStatus.FAILED;
            }

            // 查询订单状态
            Map<String, String> result = AlipayUtil.queryOrder(envConfig, paymentNo);
            String tradeStatus = result.get("trade_status");

            // 转换状态
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                return PaymentStatus.PAID;
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                return PaymentStatus.PAYING;
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                return PaymentStatus.CLOSED;
            } else {
                return PaymentStatus.FAILED;
            }

        } catch (Exception e) {
            log.error("查询支付宝支付状态失败，支付流水号：{}", paymentNo, e);
            return PaymentStatus.FAILED;
        }
    }

    @Override
    public String refund(String paymentNo, BigDecimal refundAmount, String refundReason) {
        log.info("申请支付宝退款，支付流水号：{}，退款金额：{}", paymentNo, refundAmount);

        try {
            // 获取配置
            AlipayConfig config = paymentConfigService.getAlipayConfig();
            if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
                throw new PaymentException(400, "支付宝未启用");
            }

            AlipayConfig.AlipayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null) {
                throw new PaymentException(400, "支付宝配置不完整");
            }

            // 生成退款单号
            String refundNo = "ALI_REFUND_" + System.currentTimeMillis();

            // 金额转换为字符串（支付宝使用元为单位）
            String refundAmountStr = refundAmount.toString();

            // 调用退款接口
            Map<String, String> result = AlipayUtil.refund(
                    envConfig,
                    paymentNo,
                    refundNo,
                    refundAmountStr);

            String code = result.get("code");
            if (!"10000".equals(code)) {
                String msg = result.get("msg");
                throw new PaymentException(500, "支付宝退款失败: " + msg);
            }

            return refundNo;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("申请支付宝退款失败，支付流水号：{}", paymentNo, e);
            throw new PaymentException(500, "申请支付宝退款失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Integer queryRefundStatus(String refundNo) {
        log.info("查询支付宝退款状态，退款流水号：{}", refundNo);

        try {
            // 获取配置
            AlipayConfig config = paymentConfigService.getAlipayConfig();
            if (config == null) {
                return 2; // 退款失败
            }

            AlipayConfig.AlipayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null) {
                return 2;
            }

            // TODO: 调用支付宝退款查询接口
            // 支付宝退款查询需要使用退款单号查询
            // 这里先返回占位状态，实际实现时需要调用支付宝API
            log.warn("支付宝退款状态查询尚未实现，当前为占位实现");
            return 0; // 0-退款中

        } catch (Exception e) {
            log.error("查询支付宝退款状态失败，退款流水号：{}", refundNo, e);
            return 2; // 2-退款失败
        }
    }

    @Override
    public boolean verifyRefundCallback(Object callbackData) {
        log.info("验证支付宝退款回调数据");

        // 退款回调验证逻辑与支付回调类似
        return verifyCallback(callbackData);
    }
}
