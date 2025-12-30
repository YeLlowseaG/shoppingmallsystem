package com.shoppingmall.payment.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.exception.PaymentException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.*;
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
            // 确保回调地址不为空
            if (notifyUrl == null || notifyUrl.isEmpty()) {
                throw new PaymentException(400, "微信支付回调地址不能为空");
            }
            params.put("notify_url", notifyUrl);
            params.put("trade_type", "NATIVE");

            // 生成签名
            // 调试：打印签名前的参数（用于排查签名问题）
            StringBuilder signStringBuilder = new StringBuilder();
            List<String> sortedKeysForLog = new ArrayList<>(params.keySet());
            Collections.sort(sortedKeysForLog);
            for (String k : sortedKeysForLog) {
                if (signStringBuilder.length() > 0) {
                    signStringBuilder.append("&");
                }
                signStringBuilder.append(k).append("=").append(params.get(k));
            }
            
            // 添加详细的key调试信息
            String keyForSign = config.getKey();
            log.info("=== WeChatPayUtil.createNativePayment 调试信息 ===");
            log.info("从config.getKey()获取的key值: [{}]", keyForSign);
            log.info("key的字符长度: {}", keyForSign != null ? keyForSign.length() : 0);
            log.info("key的字节长度: {}", keyForSign != null ? keyForSign.getBytes(StandardCharsets.UTF_8).length : 0);
            if (keyForSign != null && keyForSign.length() > 0) {
                // 检查每个字符
                StringBuilder charInfo = new StringBuilder();
                for (int i = 0; i < keyForSign.length(); i++) {
                    char c = keyForSign.charAt(i);
                    if (i > 0) charInfo.append(", ");
                    charInfo.append(String.format("%d:'%c'(%d)", i, c, (int)c));
                }
                log.info("key每个字符详情: {}", charInfo.toString());
            }
            
            signStringBuilder.append("&key=").append(keyForSign);
            String signString = signStringBuilder.toString();
            log.debug("微信支付签名字符串（用于调试）: {}", signString);
            log.info("签名字符串总长度: {}", signString.length());
            int keyIndex = signString.lastIndexOf("&key=");
            if (keyIndex >= 0) {
                String keyPart = signString.substring(keyIndex + 5);
                log.info("签名字符串中key部分: [{}], 长度: {}", keyPart, keyPart.length());
            }
            log.info("================================================");
            
            String sign = generateSign(params, keyForSign);
            params.put("sign", sign);
            log.debug("微信支付生成的签名: {}", sign);

            // 转换为XML
            String xmlData = mapToXml(params);

            log.info("微信支付创建订单请求: orderNo={}, amount={}", orderNo, amount);
            log.debug("微信支付请求XML: {}", xmlData);
            
            // 将XML转换为UTF-8字节数组，确保编码正确
            byte[] xmlBytes = xmlData.getBytes(StandardCharsets.UTF_8);
            log.debug("XML字节数组长度: {}", xmlBytes.length);
            
            // 发送请求 - 使用字节数组确保UTF-8编码
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("application/xml;charset=UTF-8"));
            headers.setContentLength(xmlBytes.length);
            headers.setAcceptCharset(java.util.Collections.singletonList(StandardCharsets.UTF_8));
            HttpEntity<byte[]> requestEntity = new HttpEntity<>(xmlBytes, headers);
            
            // 使用字节数组发送和接收，确保UTF-8编码
            ResponseEntity<byte[]> response = restTemplate.postForEntity(apiUrl, requestEntity, byte[].class);
            
            // 将响应字节数组转换为UTF-8字符串
            String responseBody = null;
            if (response.getBody() != null) {
                responseBody = new String(response.getBody(), StandardCharsets.UTF_8);
            }
            log.info("微信支付创建订单响应: orderNo={}, response={}", orderNo, responseBody);

            // 解析响应
            Map<String, String> result = xmlToMap(responseBody);
            String returnCode = result.get("return_code");
            String resultCode = result.get("result_code");

            if (!"SUCCESS".equals(returnCode) || !"SUCCESS".equals(resultCode)) {
                // 优先获取详细错误信息
                String errMsg = result.get("err_code_des");
                if (errMsg == null || errMsg.isEmpty()) {
                    errMsg = result.get("err_code");
                }
                if (errMsg == null || errMsg.isEmpty()) {
                    errMsg = result.get("return_msg");
                }
                if (errMsg == null || errMsg.isEmpty()) {
                    // 如果解析失败，至少显示原始响应的一部分
                    errMsg = "微信支付返回错误，原始响应: " + 
                             (responseBody != null && responseBody.length() > 200 
                              ? responseBody.substring(0, 200) + "..." 
                              : responseBody);
                }
                
                log.error("微信支付创建订单失败: return_code={}, result_code={}, err_code={}, err_code_des={}, return_msg={}, 完整响应={}", 
                          returnCode, resultCode, result.get("err_code"), result.get("err_code_des"), result.get("return_msg"), responseBody);
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

            // 检查证书路径
            String certPath = config.getCertPath();
            if (certPath == null || certPath.isEmpty()) {
                log.error("微信支付退款失败：证书路径未配置，orderNo={}, refundNo={}", orderNo, refundNo);
                throw new PaymentException(400, "微信支付证书路径未配置，无法进行退款操作");
            }

            // 发送请求（需要证书）
            log.info("开始调用微信退款API，订单号：{}，退款单号：{}，退款金额：{}分，证书路径：{}", 
                    orderNo, refundNo, refundAmount, certPath);
            
            String responseBody = sendHttpsRequestWithCert(apiUrl, xmlData, certPath, config.getMchid());
            
            log.info("微信退款API响应，订单号：{}，退款单号：{}，响应内容：{}", orderNo, refundNo, responseBody);

            // 解析响应
            Map<String, String> result = xmlToMap(responseBody);
            
            // 验证响应签名
            if (!verifySign(result, config.getKey())) {
                log.error("微信退款响应签名验证失败，订单号：{}，退款单号：{}", orderNo, refundNo);
                throw new PaymentException(500, "微信退款响应签名验证失败");
            }
            
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
            // 添加key的调试信息
            log.info("=== WeChatPayUtil.generateSign 调试信息 ===");
            log.info("generateSign方法接收到的key参数: [{}]", key);
            log.info("key参数的字符长度: {}", key != null ? key.length() : 0);
            log.info("key参数的字节长度: {}", key != null ? key.getBytes(StandardCharsets.UTF_8).length : 0);
            
            // 1. 参数排序
            List<String> keys = new ArrayList<>(params.keySet());
            Collections.sort(keys);
            
            // 添加调试：输出排序后的参数列表
            log.info("排序后的参数列表: {}", keys);

            // 2. 拼接字符串
            StringBuilder sb = new StringBuilder();
            for (String k : keys) {
                String v = params.get(k);
                if (v != null && !v.isEmpty() && !"sign".equals(k)) {
                    if (sb.length() > 0) {
                        sb.append("&");
                    }
                    sb.append(k).append("=").append(v);
                    
                    // 添加调试：输出每个参数的详细信息（特别是包含中文的参数）
                    byte[] valueBytes = v.getBytes(StandardCharsets.UTF_8);
                    if (v.length() != valueBytes.length) {
                        // 包含非ASCII字符（如中文）
                        log.info("参数 {} = [{}], 字符长度: {}, UTF-8字节长度: {}", 
                                k, v, v.length(), valueBytes.length);
                        // 输出UTF-8字节数组的十六进制表示
                        StringBuilder hexBytes = new StringBuilder();
                        for (byte b : valueBytes) {
                            if (hexBytes.length() > 0) hexBytes.append(" ");
                            hexBytes.append(String.format("%02X", b & 0xFF));
                        }
                        log.info("参数 {} 的UTF-8字节（十六进制）: {}", k, hexBytes.toString());
                    }
                }
            }
            sb.append("&key=").append(key);
            
            // 添加调试：输出用于签名的完整字符串
            String signString = sb.toString();
            byte[] signStringBytes = signString.getBytes(StandardCharsets.UTF_8);
            log.info("generateSign中用于签名的完整字符串长度: {} (字符), {} (UTF-8字节)", 
                    signString.length(), signStringBytes.length);
            int keyStartIndex = signString.lastIndexOf("&key=");
            if (keyStartIndex >= 0) {
                String keyPart = signString.substring(keyStartIndex + 5);
                log.info("generateSign中key部分: [{}], 长度: {}", keyPart, keyPart.length());
            }
            log.debug("generateSign中完整签名字符串: {}", signString);
            
            // 添加详细调试：输出签名字符串每个字符的详细信息
            // 改为debug级别，减少日志输出
            log.debug("=== 签名字符串详细分析 ===");
            log.debug("签名字符串原始内容（用于复制到在线工具）: {}", signString);
            // 只在debug级别输出每个字符的详细信息
            if (log.isDebugEnabled()) {
                log.debug("签名字符串每个字符的详细信息:");
                for (int i = 0; i < signString.length(); i++) {
                    char c = signString.charAt(i);
                    int ascii = (int) c;
                    String charType = Character.isWhitespace(c) ? " [空白字符]" : 
                                     (ascii < 32 || ascii > 126) ? " [非ASCII]" : "";
                    if (i < 50 || i >= signString.length() - 50) {
                        log.debug("位置 {}: '{}' (ASCII: {}){}", i, c, ascii, charType);
                    } else if (i == 50) {
                        log.debug("... (中间部分省略) ...");
                    }
                }
            }
            
            // 输出签名字符串的UTF-8字节数组（用于对比）- 改为debug级别
            StringBuilder signHexBytes = new StringBuilder();
            for (byte b : signStringBytes) {
                signHexBytes.append(String.format("%02X", b & 0xFF));
            }
            log.debug("签名字符串的UTF-8字节（十六进制，连续无空格，用于在线工具）: {}", signHexBytes.toString());
            log.debug("签名字符串的UTF-8字节长度: {}", signStringBytes.length);
            log.debug("=========================");

            // 3. MD5加密并转大写
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(signStringBytes);
            
            // 添加调试：输出MD5摘要的详细信息
            log.info("=== MD5计算过程 ===");
            log.info("MD5输入: UTF-8字节数组，长度: {}", signStringBytes.length);
            log.info("MD5输出: 摘要字节数组，长度: {}", digest.length);
            
            StringBuilder digestHex = new StringBuilder();
            for (byte b : digest) {
                digestHex.append(String.format("%02X", b & 0xFF));
            }
            log.debug("MD5摘要字节数组（十六进制）: {}", digestHex.toString());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            String finalSign = hexString.toString().toUpperCase();
            log.info("generateSign生成的最终签名: {}", finalSign);
            log.info("==================");
            
            // 添加验证：使用另一种方式计算MD5，确保结果一致
            try {
                MessageDigest md2 = MessageDigest.getInstance("MD5");
                md2.update(signStringBytes);
                byte[] digest2 = md2.digest();
                StringBuilder hexString2 = new StringBuilder();
                for (byte b : digest2) {
                    String hex = Integer.toHexString(0xff & b);
                    if (hex.length() == 1) {
                        hexString2.append('0');
                    }
                    hexString2.append(hex);
                }
                String finalSign2 = hexString2.toString().toUpperCase();
                if (!finalSign.equals(finalSign2)) {
                    log.error("MD5计算结果不一致！方法1: {}, 方法2: {}", finalSign, finalSign2);
                } else {
                    log.debug("MD5计算结果验证通过: {}", finalSign);
                }
            } catch (Exception e) {
                log.warn("MD5验证计算异常", e);
            }
            
            return finalSign;

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
     * 注意：按照字典序排序，确保与签名时的参数顺序一致
     */
    private static String mapToXml(Map<String, String> params) {
        StringBuilder xml = new StringBuilder("<xml>");
        // 按照字典序排序，确保与签名时的参数顺序一致
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        for (String key : keys) {
            xml.append("<").append(key).append(">");
            xml.append("<![CDATA[").append(params.get(key)).append("]]>");
            xml.append("</").append(key).append(">");
        }
        xml.append("</xml>");
        return xml.toString();
    }

    /**
     * XML转Map（修复版，支持正确解析CDATA和错误响应）
     */
    private static Map<String, String> xmlToMap(String xml) {
        Map<String, String> map = new HashMap<>();
        try {
            if (xml == null || xml.isEmpty()) {
                return map;
            }

            // 移除XML声明和根标签
            xml = xml.replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", "");
            xml = xml.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "");
            xml = xml.replace("<xml>", "").replace("</xml>", "");
            xml = xml.trim();

            // 使用正则表达式解析XML标签
            // 匹配格式：<key><![CDATA[value]]></key> 或 <key>value</key>
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("<([^>]+)>(<!\\[CDATA\\[(.*?)\\]\\]>|([^<]+))</\\1>");
            java.util.regex.Matcher matcher = pattern.matcher(xml);
            
            while (matcher.find()) {
                String key = matcher.group(1);
                String value = matcher.group(3) != null ? matcher.group(3) : matcher.group(4);
                if (value != null) {
                    map.put(key, value.trim());
                }
            }

            // 如果正则匹配失败，使用备用方法
            if (map.isEmpty()) {
                // 备用解析方法：按标签分割
                String[] tags = xml.split("</");
                for (String tag : tags) {
                    if (tag.contains("<")) {
                        int startIndex = tag.indexOf("<");
                        int endIndex = tag.indexOf(">");
                        if (startIndex >= 0 && endIndex > startIndex) {
                            String key = tag.substring(startIndex + 1, endIndex);
                            String value = "";
                            
                            // 提取值（处理CDATA）
                            int cdataStart = tag.indexOf("<![CDATA[");
                            if (cdataStart >= 0) {
                                int cdataEnd = tag.indexOf("]]>", cdataStart);
                                if (cdataEnd > cdataStart) {
                                    value = tag.substring(cdataStart + 9, cdataEnd);
                                }
                            } else {
                                // 普通标签值
                                int valueStart = endIndex + 1;
                                if (valueStart < tag.length()) {
                                    value = tag.substring(valueStart);
                                }
                            }
                            
                            if (!key.isEmpty() && !value.isEmpty()) {
                                map.put(key, value.trim());
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.error("解析XML失败: {}", xml, e);
            // 即使解析失败，也尝试返回部分信息
            if (xml.contains("return_code")) {
                try {
                    // 简单提取return_code
                    if (xml.contains("<return_code>")) {
                        int start = xml.indexOf("<return_code>");
                        if (start >= 0) {
                            start += 13; // "<return_code>".length()
                            int end = xml.indexOf("</return_code>", start);
                            if (end > start) {
                                String codePart = xml.substring(start, end);
                                // 处理CDATA
                                if (codePart.contains("<![CDATA[")) {
                                    int cdataStart = codePart.indexOf("<![CDATA[") + 9;
                                    int cdataEnd = codePart.indexOf("]]>", cdataStart);
                                    if (cdataEnd > cdataStart) {
                                        map.put("return_code", codePart.substring(cdataStart, cdataEnd));
                                    }
                                } else {
                                    map.put("return_code", codePart);
                                }
                            }
                        }
                    }
                    // 简单提取return_msg
                    if (xml.contains("<return_msg>")) {
                        int start = xml.indexOf("<return_msg>");
                        if (start >= 0) {
                            start += 12; // "<return_msg>".length()
                            int end = xml.indexOf("</return_msg>", start);
                            if (end > start) {
                                String msgPart = xml.substring(start, end);
                                // 处理CDATA
                                if (msgPart.contains("<![CDATA[")) {
                                    int cdataStart = msgPart.indexOf("<![CDATA[") + 9;
                                    int cdataEnd = msgPart.indexOf("]]>", cdataStart);
                                    if (cdataEnd > cdataStart) {
                                        map.put("return_msg", msgPart.substring(cdataStart, cdataEnd));
                                    }
                                } else {
                                    map.put("return_msg", msgPart);
                                }
                            }
                        }
                    }
                    // 提取err_code和err_code_des
                    if (xml.contains("<err_code>")) {
                        int start = xml.indexOf("<err_code>");
                        if (start >= 0) {
                            start += 10;
                            int end = xml.indexOf("</err_code>", start);
                            if (end > start) {
                                String errCode = xml.substring(start, end);
                                if (errCode.contains("<![CDATA[")) {
                                    int cdataStart = errCode.indexOf("<![CDATA[") + 9;
                                    int cdataEnd = errCode.indexOf("]]>", cdataStart);
                                    if (cdataEnd > cdataStart) {
                                        map.put("err_code", errCode.substring(cdataStart, cdataEnd));
                                    }
                                } else {
                                    map.put("err_code", errCode);
                                }
                            }
                        }
                    }
                    if (xml.contains("<err_code_des>")) {
                        int start = xml.indexOf("<err_code_des>");
                        if (start >= 0) {
                            start += 14;
                            int end = xml.indexOf("</err_code_des>", start);
                            if (end > start) {
                                String errCodeDes = xml.substring(start, end);
                                if (errCodeDes.contains("<![CDATA[")) {
                                    int cdataStart = errCodeDes.indexOf("<![CDATA[") + 9;
                                    int cdataEnd = errCodeDes.indexOf("]]>", cdataStart);
                                    if (cdataEnd > cdataStart) {
                                        map.put("err_code_des", errCodeDes.substring(cdataStart, cdataEnd));
                                    }
                                } else {
                                    map.put("err_code_des", errCodeDes);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    log.warn("提取错误信息失败", ex);
                }
            }
        }
        return map;
    }

    /**
     * 使用证书发送HTTPS请求（用于微信退款API）
     *
     * @param url 请求URL
     * @param xmlData XML请求数据
     * @param certPath 证书路径（PKCS12格式）
     * @param mchId 商户号（作为证书密码）
     * @return 响应内容
     */
    private static String sendHttpsRequestWithCert(String url, String xmlData, String certPath, String mchId) {
        InputStream certInputStream = null;
        HttpsURLConnection connection = null;
        
        try {
            // 加载证书
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            
            // 支持文件路径和classpath路径
            if (certPath.startsWith("classpath:")) {
                // 从classpath加载
                String resourcePath = certPath.substring("classpath:".length());
                certInputStream = WeChatPayUtil.class.getClassLoader().getResourceAsStream(resourcePath);
                if (certInputStream == null) {
                    throw new PaymentException(400, "证书文件不存在：" + certPath);
                }
            } else {
                // 从文件系统加载
                certInputStream = new FileInputStream(certPath);
            }
            
            // 加载证书，密码为商户号
            keyStore.load(certInputStream, mchId.toCharArray());
            
            // 创建KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, mchId.toCharArray());
            
            // 创建SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), null, new SecureRandom());
            
            // 创建URL连接
            URL requestUrl = new URL(url);
            connection = (HttpsURLConnection) requestUrl.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setConnectTimeout(30000); // 30秒连接超时
            connection.setReadTimeout(30000); // 30秒读取超时
            
            // 设置请求头
            connection.setRequestProperty("Content-Type", "application/xml;charset=UTF-8");
            connection.setRequestProperty("Content-Length", String.valueOf(xmlData.getBytes(StandardCharsets.UTF_8).length));
            
            // 发送请求数据
            try (java.io.OutputStream os = connection.getOutputStream()) {
                os.write(xmlData.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            log.info("微信退款API响应码：{}", responseCode);
            
            InputStream inputStream;
            if (responseCode >= 200 && responseCode < 300) {
                inputStream = connection.getInputStream();
            } else {
                inputStream = connection.getErrorStream();
            }
            
            if (inputStream == null) {
                throw new PaymentException(500, "微信退款API响应为空，响应码：" + responseCode);
            }
            
            // 读取响应内容
            StringBuilder response = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                response.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
            }
            
            String responseBody = response.toString();
            
            if (responseCode != 200) {
                log.error("微信退款API请求失败，响应码：{}，响应内容：{}", responseCode, responseBody);
                throw new PaymentException(500, "微信退款API请求失败，响应码：" + responseCode + "，响应内容：" + responseBody);
            }
            
            return responseBody;
            
        } catch (PaymentException e) {
            throw e;
        } catch (Exception e) {
            log.error("发送微信退款HTTPS请求异常，证书路径：{}", certPath, e);
            throw new PaymentException(500, "发送微信退款HTTPS请求异常：" + e.getMessage(), e);
        } finally {
            // 关闭资源
            if (certInputStream != null) {
                try {
                    certInputStream.close();
                } catch (Exception e) {
                    log.warn("关闭证书输入流异常", e);
                }
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}









