package com.shoppingmall.controller.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanShipCallbackDTO;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ERP回调控制器
 * 用于接收ERP系统发送的回调通知（如发货信息等）
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Slf4j
@RestController
@RequestMapping("/api/common/erp/callback")
public class ErpCallbackController {

    @Resource
    private JushuitanLogisticsService jushuitanLogisticsService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    /**
     * ERP回调地址校验接口（GET方法）
     * 聚水潭在校验回调地址时会使用GET方法访问此接口
     * 返回格式必须符合ERP平台要求：{"code":"0","msg":"执行成功"}
     *
     * @param response HTTP响应对象
     */
    @GetMapping(value = "/logistics", produces = MediaType.APPLICATION_JSON_VALUE)
    public void logisticsCallbackVerify(HttpServletResponse response) {
        try {
            log.info("收到ERP回调地址校验请求（GET方法）");
            
            // 设置响应头
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setStatus(HttpServletResponse.SC_OK);
            
            // 直接写入JSON响应，确保格式正确，字段顺序：code在前，msg在后
            String jsonResponse = "{\"code\":\"0\",\"msg\":\"执行成功\"}";
            PrintWriter writer = response.getWriter();
            writer.write(jsonResponse);
            writer.flush();
            writer.close();
            
        } catch (Exception e) {
            // 即使出现异常，也要返回正确的格式，确保聚水潭校验通过
            log.error("ERP回调地址校验异常", e);
            try {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding(StandardCharsets.UTF_8.name());
                response.setStatus(HttpServletResponse.SC_OK);
                String jsonResponse = "{\"code\":\"0\",\"msg\":\"执行成功\"}";
                PrintWriter writer = response.getWriter();
                writer.write(jsonResponse);
                writer.flush();
                writer.close();
            } catch (IOException ioException) {
                log.error("写入响应失败", ioException);
            }
        }
    }

    /**
     * ERP发货回调接口（POST方法）
     * ERP系统发货后通过此接口通知系统更新订单状态和物流信息
     * 
     * 返回格式必须符合ERP平台要求：{"code":"0","msg":"执行成功"}
     *
     * @param callbackDTO 回调数据
     * @return 标准响应格式
     */
    @PostMapping("/logistics")
    public Map<String, String> logisticsCallback(@RequestBody(required = false) JushuitanShipCallbackDTO callbackDTO) {
        // 使用LinkedHashMap保持字段顺序：code在前，msg在后
        Map<String, String> response = new LinkedHashMap<>();

        try {
            if (callbackDTO == null) {
                log.warn("ERP发货回调：请求体为空");
                response.put("code", "1");
                response.put("msg", "请求参数不能为空");
                return response;
            }

            log.info("收到ERP发货回调: orderNo={}, logisticsCompany={}, logisticsNo={}",
                    callbackDTO.getOrderNo(), callbackDTO.getLogisticsCompany(), callbackDTO.getLogisticsNo());

            // 参数验证
            if (callbackDTO.getOrderNo() == null || callbackDTO.getOrderNo().trim().isEmpty()) {
                log.error("ERP发货回调失败：订单号为空");
                response.put("code", "1");
                response.put("msg", "订单号不能为空");
                return response;
            }

            if (callbackDTO.getLogisticsCompany() == null || callbackDTO.getLogisticsCompany().trim().isEmpty()) {
                log.error("ERP发货回调失败：物流公司为空, orderNo={}", callbackDTO.getOrderNo());
                response.put("code", "1");
                response.put("msg", "物流公司不能为空");
                return response;
            }

            if (callbackDTO.getLogisticsNo() == null || callbackDTO.getLogisticsNo().trim().isEmpty()) {
                log.error("ERP发货回调失败：物流单号为空, orderNo={}", callbackDTO.getOrderNo());
                response.put("code", "1");
                response.put("msg", "物流单号不能为空");
                return response;
            }

            // 处理发货回调
            boolean success = jushuitanLogisticsService.handleShipCallback(callbackDTO);

            if (success) {
                log.info("ERP发货回调处理成功: orderNo={}", callbackDTO.getOrderNo());
                response.put("code", "0");
                response.put("msg", "执行成功");
            } else {
                log.error("ERP发货回调处理失败: orderNo={}", callbackDTO.getOrderNo());
                response.put("code", "1");
                response.put("msg", "处理失败，请查看日志");
            }

        } catch (Exception e) {
            log.error("ERP发货回调处理异常: orderNo={}", callbackDTO != null ? callbackDTO.getOrderNo() : "unknown", e);
            response.put("code", "1");
            response.put("msg", "处理异常: " + e.getMessage());
        }

        return response;
    }
}

