package com.shoppingmall.payment.util;

import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

/**
 * 支付宝工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
public class AlipayUtil {

    // 支付宝API地址
    private static final String ALIPAY_GATEWAY = "https://openapi.alipay.com/gateway.do";
    private static final String ALIPAY_SANDBOX_GATEWAY = "https://openapi.alipaydev.com/gateway.do";

    /**
     * 创建支付订单（电脑网站支付）
     *
     * @param config 支付宝配置
     * @param orderNo 商户订单号
     * @param amount 金额（元）
     * @param subject 订单标题
     * @param notifyUrl 回调地址
     * @return 支付表单HTML
     */
    public static String createPagePayment(AlipayConfig.AlipayEnvConfig config,
                                            String orderNo,
                                            String amount,
                                            String subject,
                                            String notifyUrl) {
        try {
            String gateway = config.getAppid().contains("sandbox") || config.getAppid().startsWith("2021")
                    ? ALIPAY_SANDBOX_GATEWAY
                    : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.page.pay");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new Date().toString());
            params.put("version", "1.0");
            params.put("notify_url", notifyUrl);

            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
            bizContent.put("total_amount", amount);
            bizContent.put("subject", subject);

            params.put("biz_content", mapToJson(bizContent));

            // 生成签名
            String sign = generateSign(params, config.getPrivateKey());
            params.put("sign", sign);

            // 构建表单
            return buildFormHtml(gateway, params);

        } catch (Exception e) {
            log.error("支付宝创建订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "支付宝创建订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 创建扫码支付订单
     *
     * @param config 支付宝配置
     * @param orderNo 商户订单号
     * @param amount 金额（元）
     * @param subject 订单标题
     * @param notifyUrl 回调地址
     * @return 二维码内容
     */
    public static String createQrPayment(AlipayConfig.AlipayEnvConfig config,
                                          String orderNo,
                                          String amount,
                                          String subject,
                                          String notifyUrl) {
        try {
            String gateway = config.getAppid().contains("sandbox") || config.getAppid().startsWith("2021")
                    ? ALIPAY_SANDBOX_GATEWAY
                    : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.precreate");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new Date().toString());
            params.put("version", "1.0");
            params.put("notify_url", notifyUrl);

            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("total_amount", amount);
            bizContent.put("subject", subject);

            params.put("biz_content", mapToJson(bizContent));

            // 生成签名
            String sign = generateSign(params, config.getPrivateKey());
            params.put("sign", sign);

            // TODO: 发送HTTP请求到支付宝API
            // 这里先返回占位URL，实际实现时需要调用支付宝API
            log.warn("支付宝扫码支付API调用尚未实现，当前为占位实现");
            return "https://qr.alipay.com/bax" + orderNo;

        } catch (Exception e) {
            log.error("支付宝创建扫码支付订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "支付宝创建扫码支付订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单状态
     *
     * @param config 支付宝配置
     * @param orderNo 商户订单号
     * @return 订单状态信息
     */
    public static Map<String, String> queryOrder(AlipayConfig.AlipayEnvConfig config, String orderNo) {
        try {
            String gateway = config.getAppid().contains("sandbox") || config.getAppid().startsWith("2021")
                    ? ALIPAY_SANDBOX_GATEWAY
                    : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.query");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new Date().toString());
            params.put("version", "1.0");

            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            params.put("biz_content", mapToJson(bizContent));

            // 生成签名
            String sign = generateSign(params, config.getPrivateKey());
            params.put("sign", sign);

            // TODO: 发送HTTP请求到支付宝API
            // 这里先返回占位结果
            log.warn("支付宝查询订单API调用尚未实现，当前为占位实现");
            Map<String, String> result = new HashMap<>();
            result.put("trade_status", "WAIT_BUYER_PAY");
            return result;

        } catch (Exception e) {
            log.error("支付宝查询订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "支付宝查询订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 申请退款
     *
     * @param config 支付宝配置
     * @param orderNo 商户订单号
     * @param refundNo 退款单号
     * @param refundAmount 退款金额（元）
     * @return 退款结果
     */
    public static Map<String, String> refund(AlipayConfig.AlipayEnvConfig config,
                                              String orderNo,
                                              String refundNo,
                                              String refundAmount) {
        try {
            String gateway = config.getAppid().contains("sandbox") || config.getAppid().startsWith("2021")
                    ? ALIPAY_SANDBOX_GATEWAY
                    : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.refund");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", new Date().toString());
            params.put("version", "1.0");

            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            bizContent.put("out_request_no", refundNo);
            bizContent.put("refund_amount", refundAmount);

            params.put("biz_content", mapToJson(bizContent));

            // 生成签名
            String sign = generateSign(params, config.getPrivateKey());
            params.put("sign", sign);

            // TODO: 发送HTTP请求到支付宝API
            // 这里先返回占位结果
            log.warn("支付宝退款API调用尚未实现，当前为占位实现");
            Map<String, String> result = new HashMap<>();
            result.put("code", "10000");
            result.put("msg", "Success");
            return result;

        } catch (Exception e) {
            log.error("支付宝退款异常: orderNo={}, refundNo={}", orderNo, refundNo, e);
            throw new PaymentException(500, "支付宝退款异常: " + e.getMessage(), e);
        }
    }

    /**
     * 验证回调签名
     *
     * @param params 回调参数
     * @param publicKey 支付宝公钥
     * @return 是否验证通过
     */
    public static boolean verifySign(Map<String, String> params, String publicKey) {
        try {
            String sign = params.get("sign");
            if (sign == null || sign.isEmpty()) {
                return false;
            }

            // 移除sign和sign_type参数
            Map<String, String> signParams = new HashMap<>(params);
            signParams.remove("sign");
            signParams.remove("sign_type");

            // 生成待签名字符串
            String signContent = getSignContent(signParams);

            // 验证签名
            return verify(signContent, sign, publicKey);

        } catch (Exception e) {
            log.error("支付宝签名验证异常", e);
            return false;
        }
    }

    /**
     * 生成签名
     */
    private static String generateSign(Map<String, String> params, String privateKey) {
        try {
            // 获取待签名字符串
            String signContent = getSignContent(params);

            // 使用私钥签名
            return sign(signContent, privateKey);

        } catch (Exception e) {
            log.error("生成支付宝签名异常", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 获取待签名字符串
     */
    private static String getSignContent(Map<String, String> params) {
        // 参数排序
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);

        // 拼接字符串
        StringBuilder sb = new StringBuilder();
        for (String key : keys) {
            String value = params.get(key);
            if (value != null && !value.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("&");
                }
                try {
                    sb.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    sb.append(key).append("=").append(value);
                }
            }
        }
        return sb.toString();
    }

    /**
     * RSA2签名
     */
    private static String sign(String content, String privateKey) {
        try {
            // 移除私钥的头部和尾部
            String privateKeyPEM = privateKey
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey key = keyFactory.generatePrivate(keySpec);

            // 签名
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(key);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = signature.sign();

            // Base64编码
            return Base64.getEncoder().encodeToString(signBytes);

        } catch (Exception e) {
            log.error("RSA2签名异常", e);
            throw new RuntimeException("签名失败", e);
        }
    }

    /**
     * RSA2验签
     */
    private static boolean verify(String content, String sign, String publicKey) {
        try {
            // 移除公钥的头部和尾部
            String publicKeyPEM = publicKey
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            // Base64解码
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey key = keyFactory.generatePublic(keySpec);

            // 验签
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(key);
            signature.update(content.getBytes(StandardCharsets.UTF_8));
            byte[] signBytes = Base64.getDecoder().decode(sign);

            return signature.verify(signBytes);

        } catch (Exception e) {
            log.error("RSA2验签异常", e);
            return false;
        }
    }

    /**
     * Map转JSON字符串（简单实现）
     */
    private static String mapToJson(Map<String, String> map) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (!first) {
                json.append(",");
            }
            json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
            first = false;
        }
        json.append("}");
        return json.toString();
    }

    /**
     * 构建支付表单HTML
     */
    private static String buildFormHtml(String gateway, Map<String, String> params) {
        StringBuilder html = new StringBuilder();
        html.append("<form id='alipayForm' method='post' action='").append(gateway).append("'>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            html.append("<input type='hidden' name='").append(entry.getKey())
                    .append("' value='").append(entry.getValue()).append("'/>");
        }
        html.append("</form>");
        html.append("<script>document.getElementById('alipayForm').submit();</script>");
        return html.toString();
    }
}

