package com.shoppingmall.payment.strategy.impl;

import com.shoppingmall.common.constant.PaymentStatus;
import com.shoppingmall.dto.PaymentRequestDTO;
import com.shoppingmall.dto.PaymentResponseDTO;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.payment.strategy.PaymentStrategy;
import com.shoppingmall.payment.util.WeChatPayUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * 微信支付策略实现
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WeChatPayStrategy implements PaymentStrategy {

    private final PaymentConfigService paymentConfigService;

    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO request) {
        log.info("创建微信支付订单，订单号：{}，金额：{}", request.getInternalOrderNo(), request.getAmount());

        // 获取微信支付配置
        WeChatPayConfig config = paymentConfigService.getWeChatPayConfig();
        if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
            throw new PaymentException(400, "微信支付未启用");
        }

        // 根据环境获取配置
        WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(config.getEnv())
                ? config.getProduction()
                : config.getSandbox();

        if (envConfig == null || envConfig.getAppid() == null || envConfig.getAppid().isEmpty()) {
            throw new PaymentException(400, "微信支付配置不完整，请先配置支付参数");
        }

        PaymentResponseDTO response = new PaymentResponseDTO();
        response.setInternalOrderNo(request.getInternalOrderNo());
        response.setIsMock(false);

        try {
            // 转换金额：微信支付金额单位为分
            int amountInCents = request.getAmount()
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            // 获取回调地址
            String notifyUrl = config.getNotifyUrl();
            if (notifyUrl == null || notifyUrl.isEmpty()) {
                notifyUrl = request.getNotifyUrl();
            }

            // 调用微信支付工具类创建订单
            String qrCodeUrl = WeChatPayUtil.createNativePayment(
                    envConfig,
                    request.getInternalOrderNo(),
                    amountInCents,
                    request.getDescription() != null ? request.getDescription() : "商品支付",
                    notifyUrl
            );

            response.setQrCodeUrl(qrCodeUrl);
            response.setPaymentUrl(qrCodeUrl); // 扫码支付，二维码URL就是支付URL

            log.info("微信支付订单创建成功，订单号：{}，二维码URL：{}", request.getInternalOrderNo(), qrCodeUrl);
        } catch (Exception e) {
            log.error("创建微信支付订单失败，订单号：{}", request.getInternalOrderNo(), e);
            throw new PaymentException(500, "创建微信支付订单失败：" + e.getMessage(), e);
        }

        return response;
    }

    @Override
    public boolean verifyCallback(Object callbackData) {
        log.info("验证微信支付回调数据");

        if (callbackData == null) {
            log.warn("微信支付回调数据为空");
            return false;
        }

        try {
            // 获取配置
            WeChatPayConfig config = paymentConfigService.getWeChatPayConfig();
            if (config == null) {
                log.warn("微信支付配置不存在");
                return false;
            }

            WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null || envConfig.getKey() == null || envConfig.getKey().isEmpty()) {
                log.warn("微信支付配置不完整");
                return false;
            }

            // 验证签名
            if (callbackData instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> params = (Map<String, String>) callbackData;
                return WeChatPayUtil.verifySign(params, envConfig.getKey());
            }
            log.warn("微信支付回调数据格式不正确");
            return false;

        } catch (Exception e) {
            log.error("验证微信支付回调数据失败", e);
            return false;
        }
    }

    @Override
    public Integer queryPaymentStatus(String paymentNo) {
        log.info("查询微信支付状态，支付流水号：{}", paymentNo);

        try {
            // 获取配置
            WeChatPayConfig config = paymentConfigService.getWeChatPayConfig();
            if (config == null) {
                return PaymentStatus.FAILED;
            }

            WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null) {
                return PaymentStatus.FAILED;
            }

            // 查询订单状态
            Map<String, String> result = WeChatPayUtil.queryOrder(envConfig, paymentNo);
            String tradeState = result.get("trade_state");

            // 转换状态
            if ("SUCCESS".equals(tradeState)) {
                return PaymentStatus.PAID;
            } else if ("NOTPAY".equals(tradeState) || "USERPAYING".equals(tradeState)) {
                return PaymentStatus.PAYING;
            } else if ("CLOSED".equals(tradeState) || "REVOKED".equals(tradeState)) {
                return PaymentStatus.CLOSED;
            } else {
                return PaymentStatus.FAILED;
            }

        } catch (Exception e) {
            log.error("查询微信支付状态失败，支付流水号：{}", paymentNo, e);
            return PaymentStatus.FAILED;
        }
    }

    @Override
    public String refund(String paymentNo, BigDecimal refundAmount, String refundReason) {
        log.info("申请微信支付退款，支付流水号：{}，退款金额：{}", paymentNo, refundAmount);

        try {
            // 获取配置
            WeChatPayConfig config = paymentConfigService.getWeChatPayConfig();
            if (config == null || !Boolean.TRUE.equals(config.getEnabled())) {
                throw new PaymentException(400, "微信支付未启用");
            }

            WeChatPayConfig.WeChatPayEnvConfig envConfig = "production".equals(config.getEnv())
                    ? config.getProduction()
                    : config.getSandbox();

            if (envConfig == null) {
                throw new PaymentException(400, "微信支付配置不完整");
            }

            // 转换金额：微信支付金额单位为分
            int refundAmountInCents = refundAmount
                    .multiply(new BigDecimal("100"))
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();

            // 生成退款单号
            String refundNo = "WX_REFUND_" + System.currentTimeMillis();

            // 查询原订单金额（这里需要从数据库获取，暂时使用退款金额作为总金额）
            // TODO: 从PaymentRecord获取原订单金额
            int totalAmount = refundAmountInCents;

            // 调用退款接口
            Map<String, String> result = WeChatPayUtil.refund(
                    envConfig,
                    paymentNo,
                    refundNo,
                    totalAmount,
                    refundAmountInCents
            );

            String returnCode = result.get("return_code");
            String resultCode = result.get("result_code");

            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                String errMsg = result.get("err_code_des") != null
                        ? result.get("err_code_des")
                        : result.get("return_msg");
                throw new PaymentException(500, "微信支付退款失败: " + errMsg);
            }

            return refundNo;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("申请微信支付退款失败，支付流水号：{}", paymentNo, e);
            throw new PaymentException(500, "申请微信支付退款失败：" + e.getMessage(), e);
        }
    }

    @Override
    public Integer queryRefundStatus(String refundNo) {
        log.info("查询微信支付退款状态，退款流水号：{}", refundNo);

        try {
            // TODO: 调用微信支付SDK查询退款状态
            // 这里先返回占位状态，实际实现时需要调用微信支付API
            log.warn("微信支付退款状态查询尚未实现，当前为占位实现");
            return 0; // 0-退款中

        } catch (Exception e) {
            log.error("查询微信支付退款状态失败，退款流水号：{}", refundNo, e);
            return 2; // 2-退款失败
        }
    }

    @Override
    public boolean verifyRefundCallback(Object callbackData) {
        log.info("验证微信支付退款回调数据");

        // 退款回调验证逻辑与支付回调类似
        return verifyCallback(callbackData);
    }
}

