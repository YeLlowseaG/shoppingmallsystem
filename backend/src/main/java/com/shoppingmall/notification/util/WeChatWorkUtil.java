package com.shoppingmall.notification.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * 企业微信群机器人工具类
 * 用于发送消息到企业微信群
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Slf4j
public class WeChatWorkUtil {

    private static final RestTemplate restTemplate = new RestTemplate();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 发送文本消息到企业微信群
     *
     * @param webhookUrl 企业微信群机器人Webhook URL
     * @param content    消息内容
     * @return 是否发送成功
     */
    public static boolean sendTextMessage(String webhookUrl, String content) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("企业微信Webhook URL为空，跳过发送");
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("消息内容为空，跳过发送");
            return false;
        }

        try {
            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("msgtype", "text");

            Map<String, String> textContent = new HashMap<>();
            textContent.put("content", content);
            requestBody.put("text", textContent);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送请求
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

            // 检查响应
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("企业微信消息发送成功");
                return true;
            } else {
                log.error("企业微信消息发送失败，状态码：{}, 响应：{}", response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("企业微信消息发送异常", e);
            return false;
        }
    }

    /**
     * 发送Markdown消息到企业微信群
     *
     * @param webhookUrl 企业微信群机器人Webhook URL
     * @param content    Markdown格式的消息内容
     * @return 是否发送成功
     */
    public static boolean sendMarkdownMessage(String webhookUrl, String content) {
        if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
            log.warn("企业微信Webhook URL为空，跳过发送");
            return false;
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("消息内容为空，跳过发送");
            return false;
        }

        try {
            // 构造请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("msgtype", "markdown");

            Map<String, String> markdownContent = new HashMap<>();
            markdownContent.put("content", content);
            requestBody.put("markdown", markdownContent);

            // 设置请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // 发送请求
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);

            // 检查响应
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("企业微信Markdown消息发送成功");
                return true;
            } else {
                log.error("企业微信Markdown消息发送失败，状态码：{}, 响应：{}", response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception e) {
            log.error("企业微信Markdown消息发送异常", e);
            return false;
        }
    }
}
