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
            
            // 构建同步回调地址（return_url必须指向后端接口，不能直接指向前端页面）
            // 支付宝会带着回调参数跳转到return_url，所以必须是后端接口
            String returnUrl = null;
            if (notifyUrl != null && !notifyUrl.isEmpty()) {
                try {
                    // 从notifyUrl中提取基础URL（协议+域名+端口）
                    java.net.URL url = new java.net.URL(notifyUrl);
                    String baseUrl = url.getProtocol() + "://" + url.getHost() + 
                                    (url.getPort() != -1 ? ":" + url.getPort() : "");
                    // return_url指向后端接口，后端会验证签名后重定向到前端页面
                    returnUrl = baseUrl + "/api/buyer/payment/alipay/return";
                } catch (Exception e) {
                    log.warn("无法从notifyUrl提取基础URL", e);
                    // 如果提取失败，使用notifyUrl的域名部分
                    if (notifyUrl.contains("/api/")) {
                        int apiIndex = notifyUrl.indexOf("/api/");
                        returnUrl = notifyUrl.substring(0, apiIndex) + "/api/buyer/payment/alipay/return";
                    } else {
                        returnUrl = notifyUrl.replace("/alipay/notify", "/alipay/return");
                    }
                }
            } else {
                // 如果没有notifyUrl，使用默认值
                returnUrl = "http://localhost:8081/api/buyer/payment/alipay/return";
            }
            
            log.info("支付宝return_url: {}", returnUrl);

            // 金额转换为字符串（支付宝使用元为单位）
            String amountStr = request.getAmount().toString();

            // 创建页面支付表单（用于跳转支付）
            String paymentForm = AlipayUtil.createPagePayment(
                    envConfig,
                    request.getInternalOrderNo(),
                    amountStr,
                    request.getDescription() != null ? request.getDescription() : "商品支付",
                    notifyUrl,
                    returnUrl);

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

            // 退款前先查询订单状态，确认订单是否存在且已支付成功
            log.info("退款前查询订单状态，订单号：{}", paymentNo);
            Map<String, String> orderStatus = AlipayUtil.queryOrder(envConfig, paymentNo);
            String tradeStatus = orderStatus.get("trade_status");
            
            if ("UNKNOWN".equals(tradeStatus)) {
                log.warn("无法查询到订单状态，订单号：{}，可能订单不存在", paymentNo);
                throw new PaymentException(500, "订单不存在或无法查询订单状态，请确认订单号是否正确且已支付成功");
            }
            
            // 检查订单状态是否允许退款
            if (!"TRADE_SUCCESS".equals(tradeStatus) && !"TRADE_FINISHED".equals(tradeStatus)) {
                log.warn("订单状态不允许退款，订单号：{}，订单状态：{}", paymentNo, tradeStatus);
                throw new PaymentException(500, "订单状态不允许退款，当前订单状态：" + tradeStatus + "，只有已支付成功或已完成的订单才能退款");
            }
            
            log.info("订单状态验证通过，订单号：{}，订单状态：{}，可以退款", paymentNo, tradeStatus);

            // 生成退款单号
            String refundNo = "ALI_REFUND_" + System.currentTimeMillis();

            // 金额转换为字符串（支付宝使用元为单位，需要保留两位小数）
            // 使用String.format确保格式正确，如 "1.00" 而不是 "1"
            String refundAmountStr = String.format("%.2f", refundAmount.doubleValue());

            // 调用退款接口
            Map<String, String> result = AlipayUtil.refund(
                    envConfig,
                    paymentNo,
                    refundNo,
                    refundAmountStr);

            String code = result.get("code");
            if (!"10000".equals(code)) {
                String msg = result.get("msg");
                String subMsg = result.get("sub_msg");
                String subCode = result.get("sub_code");
                
                // 构建详细的错误信息
                StringBuilder errorMsg = new StringBuilder("支付宝退款失败");
                if (subMsg != null && !subMsg.isEmpty()) {
                    errorMsg.append(": ").append(subMsg);
                } else if (msg != null && !msg.isEmpty()) {
                    errorMsg.append(": ").append(msg);
                }
                
                // 对于特定错误码，提供更详细的提示
                if ("20000".equals(code)) {
                    if ("aop.ACQ.SYSTEM_ERROR".equals(subCode)) {
                        errorMsg.append("。可能是订单不存在、订单状态不正确或支付宝系统暂时不可用，请检查订单号是否正确且已支付成功");
                    } else if ("aop.ACQ.TRADE_NOT_EXIST".equals(subCode)) {
                        errorMsg.append("。订单不存在，请确认订单号是否正确或订单是否已支付成功");
                    }
                }
                
                throw new PaymentException(500, errorMsg.toString());
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
