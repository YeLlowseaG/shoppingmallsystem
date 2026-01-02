package com.shoppingmall.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 聚水潭HTTP请求工具类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
public class JushuitanHttpUtil {

    private static final RestTemplate restTemplate = createRestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建 RestTemplate（使用系统默认SSL配置）
     */
    private static RestTemplate createRestTemplate() {
        try {
            // 使用简单的配置，不自定义SSL
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(15000);  // 15秒连接超时
            factory.setReadTimeout(30000);     // 30秒读取超时

            RestTemplate template = new RestTemplate(factory);
            log.info("RestTemplate 配置完成");

            return template;
        } catch (Exception e) {
            log.error("创建 RestTemplate 失败，使用默认配置", e);
            return new RestTemplate();
        }
    }

    /**
     * 发送POST请求到聚水潭API（使用默认的biz参数名）
     *
     * @param apiUrl API地址
     * @param appKey 应用Key
     * @param appSecret 应用Secret
     * @param accessToken 访问令牌（测试环境必填）
     * @param method 接口方法名
     * @param bizParams 业务参数（JSON字符串）
     * @return 响应结果（JSON字符串）
     */
    public static String post(String apiUrl, String appKey, String appSecret, String accessToken, String method, String bizParams) {
        return post(apiUrl, appKey, appSecret, accessToken, method, "biz", bizParams);
    }

    /**
     * 发送POST请求到聚水潭API（可指定业务参数名）
     *
     * @param apiUrl API地址
     * @param appKey 应用Key
     * @param appSecret 应用Secret
     * @param accessToken 访问令牌（测试环境必填）
     * @param method 接口方法名
     * @param bizParamName 业务参数名（如 "biz" 或 "items"）
     * @param bizParams 业务参数（JSON字符串）
     * @return 响应结果（JSON字符串）
     */
    public static String post(String apiUrl, String appKey, String appSecret, String accessToken, String method, String bizParamName, String bizParams) {
        try {
            // 1. 构建请求参数（按照聚水潭API文档要求）
            Map<String, String> params = new HashMap<>();

            // 必填参数
            if (accessToken != null && !accessToken.isEmpty()) {
                params.put("access_token", accessToken);  // 访问令牌（测试环境必填）
            }
            params.put("app_key", appKey);
            // method参数可选（使用具体路径接口时不需要）
            // 注意：只有当method不为null且不为空时才添加到params中，这样签名计算时就不会包含method
            if (method != null && !method.trim().isEmpty()) {
                params.put("method", method);
            }
            // 时间戳：UNIX时间戳（秒）
            long timestamp = java.time.Instant.now().getEpochSecond();
            log.info("使用时间戳(秒): {}", timestamp);
            params.put("timestamp", String.valueOf(timestamp));
            params.put("version", "2");  // API版本，固定为2
            params.put("charset", "utf-8");  // 字符集，固定为utf-8

            // 业务参数（参数名可以是 biz、items 等，根据具体接口而定）
            if (bizParams != null && !bizParams.isEmpty()) {
                params.put(bizParamName, bizParams);
            }

            // 2. 生成签名（签名计算时会自动排除sign字段，并按字典序排序）
            String sign = JushuitanSignUtil.generateSign(appSecret, params);
            params.put("sign", sign);

            // 3. 发送请求
            HttpHeaders headers = new HttpHeaders();
            // 聚水潭要求：application/x-www-form-urlencoded;charset=UTF-8
            headers.setContentType(MediaType.valueOf("application/x-www-form-urlencoded;charset=UTF-8"));

            // 构建表单参数（需要 URL 编码）
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(entry.getKey()).append("=")
                       .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(formData.toString(), headers);

            log.info("===== 聚水潭API完整请求信息 =====");
            log.info("请求URL: {}", apiUrl);
            if (method != null) {
                log.info("请求Method: {}", method);
            }
            log.info("请求参数(原始): {}", params);
            log.info("请求Body(URL编码后): {}", formData.toString());
            log.info("Content-Type: {}", headers.getContentType());
            log.info("=====================================");

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            String responseBody = response.getBody();
            log.info("聚水潭API响应: url={}, response={}", apiUrl, responseBody);

            return responseBody;
        } catch (Exception e) {
            log.error("聚水潭API请求失败: method={}, error={}", method, e.getMessage(), e);
            throw new RuntimeException("聚水潭API请求失败: " + e.getMessage(), e);
        }
    }

    /**
     * 解析聚水潭API响应
     *
     * @param response 响应JSON字符串
     * @param clazz 目标类型
     * @return 解析后的对象
     */
    public static <T> T parseResponse(String response, Class<T> clazz) {
        try {
            return objectMapper.readValue(response, clazz);
        } catch (Exception e) {
            log.error("解析聚水潭API响应失败: response={}, error={}", response, e.getMessage(), e);
            throw new RuntimeException("解析聚水潭API响应失败: " + e.getMessage(), e);
        }
    }
}
