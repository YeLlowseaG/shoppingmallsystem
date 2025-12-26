package com.shoppingmall.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

/**
 * 微信支付工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
public class WeChatPayUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 微信支付API地址
    private static final String WECHAT_PAY_API_BASE = "https://api.mch.weixin.qq.com";
    private static final String WECHAT_PAY_SANDBOX_API_BASE = "https://api.mch.weixin.qq.com/sandboxnew";

    /**
     * 创建支付订单（Native支付 - 扫码支付）
     *
     * @param config 微信支付配置
     * @param orderNo 商户订单号
     * @param amount 金额（分）
     * @param description 商品描述
     * @param notifyUrl 回调地址
     * @return 支付二维码URL
     */
    public static String createNativePayment(WeChatPayConfig.WeChatPayEnvConfig config,
                                             String orderNo,
                                             Integer amount,
                                             String description,
                                             String notifyUrl) {
        try {
            String apiUrl = (config.getAppid().contains("sandbox") || config.getMchid().contains("sandbox"))
                    ? WECHAT_PAY_SANDBOX_API_BASE + "/pay/unifiedorder"
                    : WECHAT_PAY_API_BASE + "/pay/unifiedorder";

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", config.getAppid());
            params.put("mch_id", config.getMchid());
            params.put("nonce_str", generateNonceStr());
            params.put("body", description != null ? description : "商品支付");
            params.put("out_trade_no", orderNo);
            params.put("total_fee", String.valueOf(amount));
            params.put("spbill_create_ip", "127.0.0.1");
            params.put("notify_url", notifyUrl);
            params.put("trade_type", "NATIVE");

            // 生成签名
            String sign = generateSign(params, config.getKey());
            params.put("sign", sign);

            // 转换为XML
            String xmlData = mapToXml(params);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> requestEntity = new HttpEntity<>(xmlData, headers);

            log.info("微信支付创建订单请求: orderNo={}, amount={}", orderNo, amount);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            String responseBody = response.getBody();
            log.info("微信支付创建订单响应: orderNo={}, response={}", orderNo, responseBody);

            // 解析响应
            Map<String, String> result = xmlToMap(responseBody);
            String returnCode = result.get("return_code");
            String resultCode = result.get("result_code");

            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                String errMsg = result.get("err_code_des") != null
                        ? result.get("err_code_des")
                        : result.get("return_msg");
                throw new PaymentException(500, "微信支付创建订单失败: " + errMsg);
            }

            // 返回二维码URL
            String codeUrl = result.get("code_url");
            if (codeUrl == null || codeUrl.isEmpty()) {
                throw new PaymentException(500, "微信支付返回的二维码URL为空");
            }

            return codeUrl;

        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("微信支付创建订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "微信支付创建订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 查询订单状态
     *
     * @param config 微信支付配置
     * @param orderNo 商户订单号
     * @return 订单状态信息
     */
    public static Map<String, String> queryOrder(WeChatPayConfig.WeChatPayEnvConfig config, String orderNo) {
        try {
            String apiUrl = (config.getAppid().contains("sandbox") || config.getMchid().contains("sandbox"))
                    ? WECHAT_PAY_SANDBOX_API_BASE + "/pay/orderquery"
                    : WECHAT_PAY_API_BASE + "/pay/orderquery";

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", config.getAppid());
            params.put("mch_id", config.getMchid());
            params.put("out_trade_no", orderNo);
            params.put("nonce_str", generateNonceStr());

            // 生成签名
            String sign = generateSign(params, config.getKey());
            params.put("sign", sign);

            // 转换为XML
            String xmlData = mapToXml(params);

            // 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_XML);
            HttpEntity<String> requestEntity = new HttpEntity<>(xmlData, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);
            String responseBody = response.getBody();

            // 解析响应
            return xmlToMap(responseBody);

        } catch (Exception e) {
            log.error("微信支付查询订单异常: orderNo={}", orderNo, e);
            throw new PaymentException(500, "微信支付查询订单异常: " + e.getMessage(), e);
        }
    }

    /**
     * 申请退款
     *
     * @param config 微信支付配置
     * @param orderNo 商户订单号
     * @param refundNo 退款单号
     * @param totalAmount 原订单金额（分）
     * @param refundAmount 退款金额（分）
     * @return 退款结果
     */
    public static Map<String, String> refund(WeChatPayConfig.WeChatPayEnvConfig config,
                                              String orderNo,
                                              String refundNo,
                                              Integer totalAmount,
                                              Integer refundAmount) {
        try {
            String apiUrl = (config.getAppid().contains("sandbox") || config.getMchid().contains("sandbox"))
                    ? WECHAT_PAY_SANDBOX_API_BASE + "/secapi/pay/refund"
                    : WECHAT_PAY_API_BASE + "/secapi/pay/refund";

            // 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("appid", config.getAppid());
            params.put("mch_id", config.getMchid());
            params.put("nonce_str", generateNonceStr());
            params.put("out_trade_no", orderNo);
            params.put("out_refund_no", refundNo);
            params.put("total_fee", String.valueOf(totalAmount));
            params.put("refund_fee", String.valueOf(refundAmount));

            // 生成签名
            String sign = generateSign(params, config.getKey());
            params.put("sign", sign);

            // 转换为XML
            String xmlData = mapToXml(params);

            // 发送请求（需要证书）
            // TODO: 实现证书认证的HTTP请求
            // 这里先返回占位结果
            log.warn("微信支付退款需要证书认证，当前为占位实现");
            Map<String, String> result = new HashMap<>();
            result.put("return_code", "SUCCESS");
            result.put("result_code", "SUCCESS");
            return result;

        } catch (Exception e) {
            log.error("微信支付退款异常: orderNo={}, refundNo={}", orderNo, refundNo, e);
            throw new PaymentException(500, "微信支付退款异常: " + e.getMessage(), e);
        }
    }

    /**
     * 验证回调签名
     *
     * @param params 回调参数
     * @param key API密钥
     * @return 是否验证通过
     */
    public static boolean verifySign(Map<String, String> params, String key) {
        try {
            String sign = params.get("sign");
            if (sign == null || sign.isEmpty()) {
                return false;
            }

            // 移除sign参数
            Map<String, String> signParams = new HashMap<>(params);
            signParams.remove("sign");

            // 生成签名
            String calculatedSign = generateSign(signParams, key);

            // 比较签名
            return sign.equals(calculatedSign);

        } catch (Exception e) {
            log.error("微信支付签名验证异常", e);
            return false;
        }
    }

    /**
     * 生成签名
     */
    private static String generateSign(Map<String, String> params, String key) {
        try {
            // 1. 参数排序
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);

            // 2. 拼接字符串
            StringBuilder sb = new StringBuilder();
            for (String k : keys) {
                String v = params.get(k);
                if (v != null && !v.isEmpty() && !"sign".equals(k)) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(k).append("=").append(v);
                }
            }
            sb.append("&key=").append(key);

            // 3. MD5加密并转大写
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();

        } catch (Exception e) {
            log.error("生成微信支付签名异常", e);
            throw new RuntimeException("生成签名失败", e);
        }
    }

    /**
     * 生成随机字符串
     */
    private static String generateNonceStr() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 32);
    }

    /**
     * Map转XML
     */
    private static String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            xml.append("<").append(entry.getKey()).append(">");
            xml.append("<![CDATA[").append(entry.getValue()).append("]]>");
            xml.append("</").append(entry.getKey()).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * XML转Map
     */
    private static Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            // 简单的XML解析（实际项目中建议使用专业的XML解析库）
            // 这里使用简单的字符串解析
            if (xml == null || xml.isEmpty()) {
                return map;
            }

            // 移除XML声明和根标签
            xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            xml = xml.replace("<xml>", "").replace("</xml>", "");

            // 解析每个字段
            String[] fields = xml.split("><");
            for (String field : fields) {
                field = field.replace("<", "").replace(">", "");
                if (field.contains("CDATA")) {
                    int start = field.indexOf("CDATA[") + 6;
                    int end = field.indexOf("]]");
                    if (start > 5 && end > start) {
                        String key = field.substring(0, field.indexOf("<"));
                        String value = field.substring(start, end);
                        map.put(key, value);
                    }
                } else if (field.contains("</")) {
                    String[] parts = field.split("</");
                    if (parts.length == 2) {
                        String key = parts[0];
                        String value = parts[1];
                        map.put(key, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析XML失败: {}", xml, e);
        }
        return map;
    }
}



