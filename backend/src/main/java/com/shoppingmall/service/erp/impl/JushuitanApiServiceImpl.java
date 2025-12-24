package com.shoppingmall.service.erp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.util.JushuitanHttpUtil;
import com.shoppingmall.dto.JushuitanLogisticsDTO;
import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.service.erp.JushuitanApiService;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚水潭API基础服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
public class JushuitanApiServiceImpl implements JushuitanApiService {

    @Resource
    private JushuitanConfigService jushuitanConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String uploadOrder(JushuitanOrderDTO orderDTO) {
        JushuitanConfigVO config = getEnabledConfigOrThrow();

        try {
            // 将订单DTO转换为JSON字符串
            String bizContent = objectMapper.writeValueAsString(orderDTO);

            // 调用聚水潭订单上传接口
            String response = JushuitanHttpUtil.post(
                config.getApiUrl(),
                config.getAppKey(),
                config.getAppSecret(),
                "orders.upload",
                bizContent
            );

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            Integer code = jsonNode.get("code").asInt();

            if (code != 0) {
                String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知错误";
                throw new RuntimeException("订单上传失败: " + msg);
            }

            // 返回聚水潭订单ID
            JsonNode data = jsonNode.get("data");
            if (data != null && data.has("so_id")) {
                return data.get("so_id").asText();
            }

            return orderDTO.getSoId();
        } catch (Exception e) {
            log.error("上传订单到聚水潭失败: {}", orderDTO.getSoId(), e);
            throw new RuntimeException("上传订单到聚水潭失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String queryOrderStatus(String soId) {
        JushuitanConfigVO config = getEnabledConfigOrThrow();

        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("so_id", soId);
            String bizContent = objectMapper.writeValueAsString(params);

            // 调用聚水潭订单查询接口
            String response = JushuitanHttpUtil.post(
                config.getApiUrl(),
                config.getAppKey(),
                config.getAppSecret(),
                "orders.single.query",
                bizContent
            );

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            Integer code = jsonNode.get("code").asInt();

            if (code != 0) {
                String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知错误";
                throw new RuntimeException("查询订单状态失败: " + msg);
            }

            // 返回订单状态
            JsonNode data = jsonNode.get("data");
            if (data != null && data.has("status")) {
                return data.get("status").asText();
            }

            return "UNKNOWN";
        } catch (Exception e) {
            log.error("查询订单状态失败: {}", soId, e);
            throw new RuntimeException("查询订单状态失败: " + e.getMessage(), e);
        }
    }

    @Override
    public List<JushuitanLogisticsDTO> queryLogistics(String soId) {
        List<String> soIds = new ArrayList<>();
        soIds.add(soId);
        return batchQueryLogistics(soIds);
    }

    @Override
    public List<JushuitanLogisticsDTO> batchQueryLogistics(List<String> soIds) {
        JushuitanConfigVO config = getEnabledConfigOrThrow();

        try {
            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("so_ids", String.join(",", soIds));
            String bizContent = objectMapper.writeValueAsString(params);

            // 调用聚水潭物流查询接口
            String response = JushuitanHttpUtil.post(
                config.getApiUrl(),
                config.getAppKey(),
                config.getAppSecret(),
                "logistic.query",
                bizContent
            );

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            Integer code = jsonNode.get("code").asInt();

            if (code != 0) {
                String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知错误";
                throw new RuntimeException("查询物流信息失败: " + msg);
            }

            // 解析物流数据
            List<JushuitanLogisticsDTO> logisticsList = new ArrayList<>();
            JsonNode data = jsonNode.get("data");

            if (data != null && data.isArray()) {
                for (JsonNode item : data) {
                    JushuitanLogisticsDTO dto = new JushuitanLogisticsDTO();

                    if (item.has("so_id")) {
                        dto.setSoId(item.get("so_id").asText());
                    }
                    if (item.has("lc_id")) {
                        dto.setLogisticsCode(item.get("lc_id").asText());
                    }
                    if (item.has("logistics_company")) {
                        dto.setLogisticsCompany(item.get("logistics_company").asText());
                    }
                    if (item.has("l_id")) {
                        dto.setLogisticsNo(item.get("l_id").asText());
                    }
                    if (item.has("send_date")) {
                        dto.setSendDate(item.get("send_date").asText());
                    }

                    logisticsList.add(dto);
                }
            }

            return logisticsList;
        } catch (Exception e) {
            log.error("查询物流信息失败: {}", soIds, e);
            throw new RuntimeException("查询物流信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEnabled() {
        JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
        return config != null;
    }

    /**
     * 获取启用的配置，如果未启用则抛出异常
     */
    private JushuitanConfigVO getEnabledConfigOrThrow() {
        JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
        if (config == null) {
            throw new RuntimeException("聚水潭配置未启用或不存在");
        }
        return config;
    }
}
