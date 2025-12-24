package com.shoppingmall.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送POST请求到聚水潭API
     *
     * @param apiUrl API地址
     * @param appKey 应用Key
     * @param appSecret 应用Secret
     * @param method 接口方法名
     * @param bizParams 业务参数（JSON字符串）
     * @return 响应结果（JSON字符串）
     */
    public static String post(String apiUrl, String appKey, String appSecret, String method, String bizParams) {
        try {
            // 1. 构建请求参数
            Map<String, String> params = new HashMap<>();
            params.put("app_key", appKey);
            params.put("method", method);
            params.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));

            if (bizParams != null && !bizParams.isEmpty()) {
                params.put("biz_content", bizParams);
            }

            // 2. 生成签名
            String sign = JushuitanSignUtil.generateSign(appSecret, params);
            params.put("sign", sign);

            // 3. 发送请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // 构建表单参数
            StringBuilder formData = new StringBuilder();
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (formData.length() > 0) {
                    formData.append("&");
                }
                formData.append(entry.getKey()).append("=").append(entry.getValue());
            }

            HttpEntity<String> requestEntity = new HttpEntity<>(formData.toString(), headers);

            log.info("聚水潭API请求: method={}, url={}, params={}", method, apiUrl, params);

            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, requestEntity, String.class);

            String responseBody = response.getBody();
            log.info("聚水潭API响应: method={}, response={}", method, responseBody);

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
