package com.shoppingmall.controller.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanShipCallbackDTO;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.HashMap;
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
     * ERP发货回调接口
     * ERP系统发货后通过此接口通知系统更新订单状态和物流信息
     * 
     * 返回格式必须符合ERP平台要求：{"code":"0","msg":"执行成功"}
     *
     * @param callbackDTO 回调数据
     * @return 标准响应格式
     */
    @PostMapping("/logistics")
    public Map<String, String> logisticsCallback(@RequestBody JushuitanShipCallbackDTO callbackDTO) {
        log.info("收到ERP发货回调: orderNo={}, logisticsCompany={}, logisticsNo={}",
                callbackDTO.getOrderNo(), callbackDTO.getLogisticsCompany(), callbackDTO.getLogisticsNo());

        Map<String, String> response = new HashMap<>();

        try {
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

