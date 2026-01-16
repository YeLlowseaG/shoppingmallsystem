package com.shoppingmall.service.erp.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.util.JushuitanHttpUtil;
import com.shoppingmall.dto.JushuitanLogisticsDTO;
import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.dto.JushuitanUploadOrderResponseDTO;
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

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public JushuitanUploadOrderResponseDTO uploadOrder(JushuitanOrderDTO orderDTO) {
        JushuitanConfigVO config = getEnabledConfigOrThrow();

        try {
            // 将订单DTO直接放入数组中（biz参数的值直接是数组，不是对象）
            List<JushuitanOrderDTO> orders = new ArrayList<>();
            orders.add(orderDTO);
            
            // 直接序列化为数组，不要包装在对象中
            String bizContent = objectMapper.writeValueAsString(orders);

            // 使用配置中的API地址（去除前后空格）
            String apiUrl = config.getApiUrl().trim();
            
            // 如果配置的是通用接口路径（/api/open/query.aspx），则替换为订单上传专用路径
            if (apiUrl.contains("/api/open/query.aspx")) {
                if ("test".equals(config.getEnvType())) {
                    apiUrl = "https://dev-api.jushuitan.com/open/jushuitan/orders/upload";
                } else {
                    apiUrl = "https://openapi.jushuitan.com/open/jushuitan/orders/upload";
                }
            }

            log.info("===== 聚水潭订单推送开始 =====");
            log.info("订单号: {}", orderDTO.getSoId());
            log.info("API URL: {}", apiUrl);
            log.info("App Key: {}", config.getAppKey());
            log.info("Access Token: {}", config.getAccessToken() != null ? config.getAccessToken().substring(0, 8) + "****" : "null");
            log.info("业务数据(biz): {}", bizContent);

            // 调用聚水潭订单上传接口（使用专用路径，不需要method参数）
            String response = JushuitanHttpUtil.post(
                apiUrl,
                config.getAppKey(),
                config.getAppSecret(),
                config.getAccessToken(),
                null,  // 使用专用路径时，method参数传null
                "biz",  // 参数名是biz
                bizContent
            );

            log.info("聚水潭API响应: {}", response);

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            Integer code = jsonNode.get("code").asInt();

            // 创建响应DTO
            JushuitanUploadOrderResponseDTO responseDTO = new JushuitanUploadOrderResponseDTO();
            responseDTO.setResponseJson(response);
            responseDTO.setSuccess(code == 0);

            if (code != 0) {
                String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知错误";
                log.error("订单推送失败 - code: {}, msg: {}, response: {}", code, msg, response);
                responseDTO.setMessage(msg);
                throw new RuntimeException("订单上传失败: " + msg);
            }

            // 提取响应数据
            JsonNode dataNode = jsonNode.get("data");
            if (dataNode != null) {
                // 处理响应格式：data.datas[0] 或 data.so_id
                JsonNode datasArray = dataNode.get("datas");
                if (datasArray != null && datasArray.isArray() && datasArray.size() > 0) {
                    JsonNode firstData = datasArray.get(0);
                    if (firstData.has("so_id")) {
                        responseDTO.setErpOrderId(firstData.get("so_id").asText());
                    }
                    if (firstData.has("o_id")) {
                        responseDTO.setErpInternalOrderId(String.valueOf(firstData.get("o_id").asLong()));
                    }
                    if (firstData.has("msg")) {
                        responseDTO.setMessage(firstData.get("msg").asText());
                    }
                } else if (dataNode.has("so_id")) {
                    // 兼容旧格式：直接有so_id
                    responseDTO.setErpOrderId(dataNode.get("so_id").asText());
                }
            }

            // 如果没有提取到ERP订单ID，使用原始订单号
            if (responseDTO.getErpOrderId() == null || responseDTO.getErpOrderId().isEmpty()) {
                responseDTO.setErpOrderId(orderDTO.getSoId());
            }

            return responseDTO;
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
                config.getAccessToken(),
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
                config.getAccessToken(),
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

    @Override
    public String cancelOrderByInternalId(List<Integer> oIds, String cancelType, String remark) {
        JushuitanConfigVO config = getEnabledConfigOrThrow();

        try {
            // 构建请求参数
            Map<String, Object> params = new HashMap<>();
            params.put("o_ids", oIds);
            params.put("cancel_type", cancelType);
            if (remark != null && !remark.isEmpty()) {
                params.put("remark", remark);
            }
            String bizContent = objectMapper.writeValueAsString(params);

            // 确定API地址
            String apiUrl;
            if ("test".equals(config.getEnvType())) {
                apiUrl = "https://dev-api.jushuitan.com/open/jushuitan/orderbyoid/cancel";
            } else {
                apiUrl = "https://openapi.jushuitan.com/open/jushuitan/orderbyoid/cancel";
            }

            log.info("===== 聚水潭订单取消开始 =====");
            log.info("内部订单号列表: {}", oIds);
            log.info("取消类型: {}", cancelType);
            log.info("备注: {}", remark);
            log.info("API URL: {}", apiUrl);
            log.info("业务数据(biz): {}", bizContent);

            // 调用聚水潭订单取消接口（使用专用路径，不需要method参数）
            String response = JushuitanHttpUtil.post(
                apiUrl,
                config.getAppKey(),
                config.getAppSecret(),
                config.getAccessToken(),
                null,  // 使用专用路径时，method参数传null
                "biz",  // 参数名是biz
                bizContent
            );

            log.info("聚水潭订单取消API响应: {}", response);

            // 解析响应
            JsonNode jsonNode = objectMapper.readTree(response);
            Integer code = jsonNode.get("code").asInt();

            if (code != 0) {
                String msg = jsonNode.has("msg") ? jsonNode.get("msg").asText() : "未知错误";
                log.error("订单取消失败 - code: {}, msg: {}, response: {}", code, msg, response);
                throw new RuntimeException("订单取消失败: " + msg);
            }

            log.info("订单取消成功: oIds={}", oIds);
            return response;
        } catch (Exception e) {
            log.error("取消订单失败: oIds={}", oIds, e);
            throw new RuntimeException("取消订单失败: " + e.getMessage(), e);
        }
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
