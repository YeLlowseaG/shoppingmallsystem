package com.shoppingmall.payment.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

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

    // HTTP客户端
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    // JSON解析器
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 创建支付订单（电脑网站支付）
     *
     * @param config    支付宝配置
     * @param orderNo   商户订单号
     * @param amount    金额（元）
     * @param subject   订单标题
     * @param notifyUrl 回调地址
     * @return 支付表单HTML
     */
    public static String createPagePayment(AlipayConfig.AlipayEnvConfig config,
            String orderNo,
            String amount,
            String subject,
            String notifyUrl) {
        try {
            String gateway = "sandbox".equals(config.getEnv()) ? ALIPAY_SANDBOX_GATEWAY : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.page.pay");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", formatTimestamp(new Date()));
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
     * @param config    支付宝配置
     * @param orderNo   商户订单号
     * @param amount    金额（元）
     * @param subject   订单标题
     * @param notifyUrl 回调地址
     * @return 二维码内容
     */
    public static String createQrPayment(AlipayConfig.AlipayEnvConfig config,
            String orderNo,
            String amount,
            String subject,
            String notifyUrl) {
        try {
            String gateway = "sandbox".equals(config.getEnv()) ? ALIPAY_SANDBOX_GATEWAY : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.precreate");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", formatTimestamp(new Date()));
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

            // 发送HTTP请求到支付宝API
            try {
                String responseBody = sendHttpRequest(gateway, params);

                // 解析响应
                JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
                JsonNode precreateResponse = responseJson.get("alipay_trade_precreate_response");

                if (precreateResponse != null) {
                    String code = precreateResponse.get("code") != null ? precreateResponse.get("code").asText() : "";
                    if ("10000".equals(code)) {
                        // 成功，返回二维码内容
                        String qrCode = precreateResponse.get("qr_code") != null
                                ? precreateResponse.get("qr_code").asText()
                                : "";
                        log.info("支付宝扫码支付订单创建成功，订单号：{}，二维码：{}", orderNo, qrCode);
                        return qrCode;
                    } else {
                        String msg = precreateResponse.get("msg") != null
                                ? precreateResponse.get("msg").asText()
                                : "未知错误";
                        String subMsg = precreateResponse.get("sub_msg") != null
                                ? precreateResponse.get("sub_msg").asText()
                                : "";
                        log.error("支付宝扫码支付订单创建失败，订单号：{}，错误码：{}，错误信息：{}，子错误信息：{}",
                                orderNo, code, msg, subMsg);
                        throw new PaymentException(500, "支付宝扫码支付失败：" + msg + (subMsg.isEmpty() ? "" : " - " + subMsg));
                    }
                } else {
                    log.error("支付宝扫码支付响应格式错误，订单号：{}，响应：{}", orderNo, responseBody);
                    throw new PaymentException(500, "支付宝扫码支付响应格式错误");
                }
            } catch (PaymentException e) {
                throw e;
            } catch (Exception e) {
                log.error("支付宝扫码支付HTTP请求异常，订单号：{}", orderNo, e);
                throw new PaymentException(500, "支付宝扫码支付请求异常：" + e.getMessage(), e);
            }

        } catch (Exception e) {
            log.error("支付宝创建扫码支付订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "支付宝创建扫码支付订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单状态
     *
     * @param config  支付宝配置
     * @param orderNo 商户订单号
     * @return 订单状态信息
     */
    public static Map<String, String> queryOrder(AlipayConfig.AlipayEnvConfig config, String orderNo) {
        try {
            String gateway = "sandbox".equals(config.getEnv()) ? ALIPAY_SANDBOX_GATEWAY : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.query");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", formatTimestamp(new Date()));
            params.put("version", "1.0");

            // 业务参数
            Map<String, String> bizContent = new HashMap<>();
            bizContent.put("out_trade_no", orderNo);
            params.put("biz_content", mapToJson(bizContent));

            // 生成签名
            String sign = generateSign(params, config.getPrivateKey());
            params.put("sign", sign);

            // 发送HTTP请求到支付宝API
            try {
                String responseBody = sendHttpRequest(gateway, params);

                // 解析响应
                JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
                JsonNode queryResponse = responseJson.get("alipay_trade_query_response");

                if (queryResponse != null) {
                    String code = queryResponse.get("code") != null ? queryResponse.get("code").asText() : "";
                    if ("10000".equals(code)) {
                        // 成功，返回订单状态
                        Map<String, String> result = new HashMap<>();
                        String tradeStatus = queryResponse.get("trade_status") != null
                                ? queryResponse.get("trade_status").asText()
                                : "UNKNOWN";
                        result.put("trade_status", tradeStatus);
                        log.info("支付宝查询订单成功，订单号：{}，状态：{}", orderNo, tradeStatus);
                        return result;
                    } else {
                        String msg = queryResponse.get("msg") != null
                                ? queryResponse.get("msg").asText()
                                : "未知错误";
                        log.warn("支付宝查询订单失败，订单号：{}，错误码：{}，错误信息：{}", orderNo, code, msg);
                        // 查询失败时返回未知状态
                        Map<String, String> result = new HashMap<>();
                        result.put("trade_status", "UNKNOWN");
                        return result;
                    }
                } else {
                    log.error("支付宝查询订单响应格式错误，订单号：{}，响应：{}", orderNo, responseBody);
                    Map<String, String> result = new HashMap<>();
                    result.put("trade_status", "UNKNOWN");
                    return result;
                }
            } catch (Exception e) {
                log.error("支付宝查询订单HTTP请求异常，订单号：{}", orderNo, e);
                Map<String, String> result = new HashMap<>();
                result.put("trade_status", "UNKNOWN");
                return result;
            }

        } catch (Exception e) {
            log.error("支付宝查询订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "支付宝查询订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 申请退款
     *
     * @param config       支付宝配置
     * @param orderNo      商户订单号
     * @param refundNo     退款单号
     * @param refundAmount 退款金额（元）
     * @return 退款结果
     */
    public static Map<String, String> refund(AlipayConfig.AlipayEnvConfig config,
            String orderNo,
            String refundNo,
            String refundAmount) {
        try {
            String gateway = "sandbox".equals(config.getEnv()) ? ALIPAY_SANDBOX_GATEWAY : ALIPAY_GATEWAY;

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_id", config.getAppid());
            params.put("method", "alipay.trade.refund");
            params.put("charset", "utf-8");
            params.put("sign_type", "RSA2");
            params.put("timestamp", formatTimestamp(new Date()));
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

            // 发送HTTP请求到支付宝API
            try {
                String responseBody = sendHttpRequest(gateway, params);

                // 解析响应
                JsonNode responseJson = OBJECT_MAPPER.readTree(responseBody);
                JsonNode refundResponse = responseJson.get("alipay_trade_refund_response");

                if (refundResponse != null) {
                    String code = refundResponse.get("code") != null ? refundResponse.get("code").asText() : "";
                    String msg = refundResponse.get("msg") != null ? refundResponse.get("msg").asText() : "";
                    String subMsg = refundResponse.get("sub_msg") != null ? refundResponse.get("sub_msg").asText() : "";

                    Map<String, String> result = new HashMap<>();
                    result.put("code", code);
                    result.put("msg", msg);
                    if (!subMsg.isEmpty()) {
                        result.put("sub_msg", subMsg);
                    }

                    if ("10000".equals(code)) {
                        log.info("支付宝退款成功，订单号：{}，退款单号：{}，退款金额：{}", orderNo, refundNo, refundAmount);
                    } else {
                        log.error("支付宝退款失败，订单号：{}，退款单号：{}，错误码：{}，错误信息：{}，子错误信息：{}",
                                orderNo, refundNo, code, msg, subMsg);
                    }

                    return result;
                } else {
                    log.error("支付宝退款响应格式错误，订单号：{}，退款单号：{}，响应：{}", orderNo, refundNo, responseBody);
                    Map<String, String> result = new HashMap<>();
                    result.put("code", "40004");
                    result.put("msg", "响应格式错误");
                    return result;
                }
            } catch (Exception e) {
                log.error("支付宝退款HTTP请求异常，订单号：{}，退款单号：{}", orderNo, refundNo, e);
                Map<String, String> result = new HashMap<>();
                result.put("code", "40004");
                result.put("msg", "请求异常：" + e.getMessage());
                return result;
            }

        } catch (Exception e) {
            log.error("支付宝退款异常: orderNo={}, refundNo={}", orderNo, refundNo, e);
            throw new PaymentException(500, "支付宝退款异常: " + e.getMessage(), e);
        }
    }

    /**
     * 验证回调签名
     *
     * @param params    回调参数
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
                    .append("' value='").append(escapeHtml(entry.getValue())).append("'/>");
        }
        html.append("</form>");
        html.append("<script>document.getElementById('alipayForm').submit();</script>");
        return html.toString();
    }

    /**
     * 发送HTTP请求到支付宝API
     */
    private static String sendHttpRequest(String gateway, Map<String, String> params) throws Exception {
        // 构建请求参数（URL编码）
        String requestBody = params.entrySet().stream()
                .map(entry -> {
                    try {
                        return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .collect(Collectors.joining("&"));

        int maxAttempts = 3;
        Duration requestTimeout = Duration.ofSeconds(60);

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(java.net.URI.create(gateway))
                        .header("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8")
                        .timeout(requestTimeout)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                        .build();

                HttpResponse<String> response = HTTP_CLIENT.send(request,
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

                if (response.statusCode() != 200) {
                    throw new PaymentException(500, "支付宝API请求失败，HTTP状态码：" + response.statusCode());
                }

                return response.body();
            } catch (java.io.IOException e) {
                log.warn("支付宝扫码支付HTTP请求第{}次尝试失败: {}", attempt, e.getMessage());
                if (attempt == maxAttempts) {
                    log.error("支付宝扫码支付HTTP请求最终失败，订单网关：{}，请求体长度：{}", gateway, requestBody.length());
                    throw new PaymentException(500, "支付宝扫码支付请求异常：" + e.getMessage(), e);
                }
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        throw new PaymentException(500, "支付宝扫码支付请求异常：重试失败");
    }

    /**
     * HTML转义
     */
    private static String escapeHtml(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * 格式化时间戳（支付宝要求格式：yyyy-MM-dd HH:mm:ss）
     */
    private static String formatTimestamp(Date date) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getTimeZone("GMT+8"));
        return sdf.format(date);
    }
}
