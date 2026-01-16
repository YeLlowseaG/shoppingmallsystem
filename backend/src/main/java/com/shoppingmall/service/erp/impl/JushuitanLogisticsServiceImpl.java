package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanLogisticsDTO;
import com.shoppingmall.dto.JushuitanShipCallbackDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderLogistics;
import com.shoppingmall.entity.OrderSyncLog;
import com.shoppingmall.mapper.OrderLogisticsMapper;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.mapper.OrderSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanApiService;
import com.shoppingmall.service.erp.JushuitanLogisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 聚水潭物流回传服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
public class JushuitanLogisticsServiceImpl implements JushuitanLogisticsService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private OrderLogisticsMapper orderLogisticsMapper;

    @Resource
    private OrderSyncLogMapper orderSyncLogMapper;

    @Resource
    private JushuitanApiService jushuitanApiService;

    @Resource
    private com.shoppingmall.service.erp.JushuitanConfigService jushuitanConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pullLogistics(Long orderId) {
        // 检查ERP是否启用
        if (!jushuitanApiService.isEnabled()) {
            log.warn("聚水潭配置未启用，跳过物流拉取: orderId={}", orderId);
            return false;
        }

        // 查询订单信息
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            return false;
        }

        // 检查订单是否已同步到ERP
        if (order.getErpSyncStatus() != 1) {
            log.warn("订单未同步到ERP，无法拉取物流: orderId={}, erpSyncStatus={}", orderId, order.getErpSyncStatus());
            return false;
        }

        // 获取当前ERP配置的环境类型和店铺ID（参考订单上传逻辑）
        String envType = "production"; // 默认生产环境
        String shopId = null;
        try {
            com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
            if (currentConfig != null) {
                if (currentConfig.getEnvType() != null) {
                    envType = currentConfig.getEnvType();
                }
                // 根据环境类型获取对应的店铺ID
                shopId = "test".equals(envType) 
                    ? currentConfig.getTestShopId() 
                    : currentConfig.getShopId();
                // 空字符串转为 null，避免发送空值给聚水潭API
                if (shopId != null && shopId.trim().isEmpty()) {
                    shopId = null;
                }
            }
        } catch (Exception e) {
            log.warn("获取ERP配置信息失败，使用默认值", e);
        }

        // 记录同步日志
        OrderSyncLog syncLog = new OrderSyncLog();
        syncLog.setOrderId(orderId);
        syncLog.setOrderNo(order.getOrderNo());
        syncLog.setEnvType(envType); // 记录环境类型
        syncLog.setSyncType("PULL_LOGISTICS");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            // 构建实际发送给聚水潭的完整请求参数
            Map<String, Object> apiRequestParams = new HashMap<>();
            apiRequestParams.put("so_ids", Arrays.asList(order.getOrderNo()));
            
            // 如果配置了店铺ID，添加到请求参数中
            if (shopId != null && !shopId.trim().isEmpty()) {
                try {
                    apiRequestParams.put("shop_id", Integer.parseInt(shopId));
                } catch (NumberFormatException e) {
                    log.warn("店铺ID格式错误，无法转换为Integer: {}", shopId);
                }
            }
            
            // 记录完整的API请求参数到日志
            syncLog.setRequestData(objectMapper.writeValueAsString(apiRequestParams));

            // 从聚水潭查询物流信息（传递店铺ID）
            // 使用带店铺ID的方法获取完整响应
            com.shoppingmall.dto.JushuitanLogisticsQueryResponseDTO responseDTO = 
                ((com.shoppingmall.service.erp.impl.JushuitanApiServiceImpl) jushuitanApiService)
                    .batchQueryLogistics(Arrays.asList(order.getOrderNo()), shopId);

            // 保存完整响应JSON到日志（参考订单上传逻辑）
            syncLog.setResponseData(responseDTO.getResponseJson());

            // 获取解析后的物流列表
            List<JushuitanLogisticsDTO> logisticsList = responseDTO.getLogisticsList();

            if (logisticsList == null || logisticsList.isEmpty()) {
                log.info("订单暂无物流信息: orderId={}, orderNo={}", orderId, order.getOrderNo());
                syncLog.setSyncStatus(1); // 成功（但无数据）
                return true;
            }

            // 取第一条物流信息（通常一个订单对应一个物流）
            JushuitanLogisticsDTO logisticsDTO = logisticsList.get(0);

            // 检查是否已存在物流记录
            OrderLogistics existingLogistics = orderLogisticsMapper.selectOne(
                new QueryWrapper<OrderLogistics>().eq("order_id", orderId)
            );

            if (existingLogistics == null) {
                // 新增物流记录
                OrderLogistics logistics = new OrderLogistics();
                logistics.setOrderId(orderId);
                logistics.setLogisticsCompany(logisticsDTO.getLogisticsCompany());
                logistics.setLogisticsNo(logisticsDTO.getLogisticsNo());

                // 解析发货时间
                if (logisticsDTO.getSendDate() != null && !logisticsDTO.getSendDate().isEmpty()) {
                    try {
                        logistics.setShippingTime(LocalDateTime.parse(logisticsDTO.getSendDate(), dateTimeFormatter));
                    } catch (Exception e) {
                        log.warn("解析发货时间失败: {}", logisticsDTO.getSendDate(), e);
                    }
                }

                // 构建完整的物流信息JSON对象，保存到 tracking_info 字段
                Map<String, Object> trackingInfo = new HashMap<>();
                if (logisticsDTO.getOId() != null) {
                    trackingInfo.put("erpInternalOrderId", logisticsDTO.getOId());
                }
                if (logisticsDTO.getShopId() != null) {
                    trackingInfo.put("shopId", logisticsDTO.getShopId());
                }
                if (logisticsDTO.getFreight() != null) {
                    trackingInfo.put("freight", logisticsDTO.getFreight());
                }
                if (logisticsDTO.getWeight() != null) {
                    trackingInfo.put("weight", logisticsDTO.getWeight());
                }
                if (logisticsDTO.getWmsCoId() != null) {
                    trackingInfo.put("wmsCoId", logisticsDTO.getWmsCoId());
                }
                if (logisticsDTO.getLogisticsCode() != null) {
                    trackingInfo.put("logisticsCode", logisticsDTO.getLogisticsCode());
                }
                if (logisticsDTO.getAsId() != null) {
                    trackingInfo.put("asId", logisticsDTO.getAsId());
                }
                if (logisticsDTO.getItems() != null && !logisticsDTO.getItems().isEmpty()) {
                    trackingInfo.put("items", logisticsDTO.getItems());
                }

                // 保存到 tracking_info 字段
                if (!trackingInfo.isEmpty()) {
                    logistics.setTrackingInfo(objectMapper.writeValueAsString(trackingInfo));
                }

                orderLogisticsMapper.insert(logistics);

                // 更新订单状态为已发货
                if (order.getOrderStatus() == 1) { // 已付款未发货
                    order.setOrderStatus(2); // 已发货
                    order.setShipTime(logistics.getShippingTime());
                    orderRepository.updateById(order);
                }

                log.info("物流信息拉取成功（新增）: orderId={}, logisticsNo={}", orderId, logisticsDTO.getLogisticsNo());
            } else {
                // 更新物流记录
                existingLogistics.setLogisticsCompany(logisticsDTO.getLogisticsCompany());
                existingLogistics.setLogisticsNo(logisticsDTO.getLogisticsNo());

                // 解析发货时间
                if (logisticsDTO.getSendDate() != null && !logisticsDTO.getSendDate().isEmpty()) {
                    try {
                        existingLogistics.setShippingTime(LocalDateTime.parse(logisticsDTO.getSendDate(), dateTimeFormatter));
                    } catch (Exception e) {
                        log.warn("解析发货时间失败: {}", logisticsDTO.getSendDate(), e);
                    }
                }

                // 构建完整的物流信息JSON对象，更新 tracking_info 字段
                Map<String, Object> trackingInfo = new HashMap<>();
                if (logisticsDTO.getOId() != null) {
                    trackingInfo.put("erpInternalOrderId", logisticsDTO.getOId());
                }
                if (logisticsDTO.getShopId() != null) {
                    trackingInfo.put("shopId", logisticsDTO.getShopId());
                }
                if (logisticsDTO.getFreight() != null) {
                    trackingInfo.put("freight", logisticsDTO.getFreight());
                }
                if (logisticsDTO.getWeight() != null) {
                    trackingInfo.put("weight", logisticsDTO.getWeight());
                }
                if (logisticsDTO.getWmsCoId() != null) {
                    trackingInfo.put("wmsCoId", logisticsDTO.getWmsCoId());
                }
                if (logisticsDTO.getLogisticsCode() != null) {
                    trackingInfo.put("logisticsCode", logisticsDTO.getLogisticsCode());
                }
                if (logisticsDTO.getAsId() != null) {
                    trackingInfo.put("asId", logisticsDTO.getAsId());
                }
                if (logisticsDTO.getItems() != null && !logisticsDTO.getItems().isEmpty()) {
                    trackingInfo.put("items", logisticsDTO.getItems());
                }

                // 更新 tracking_info 字段
                if (!trackingInfo.isEmpty()) {
                    existingLogistics.setTrackingInfo(objectMapper.writeValueAsString(trackingInfo));
                }

                orderLogisticsMapper.updateById(existingLogistics);

                // 更新订单状态为已发货
                if (order.getOrderStatus() == 1) { // 已付款未发货
                    order.setOrderStatus(2); // 已发货
                    order.setShipTime(existingLogistics.getShippingTime());
                    orderRepository.updateById(order);
                }

                log.info("物流信息拉取成功（更新）: orderId={}, logisticsNo={}", orderId, logisticsDTO.getLogisticsNo());
            }

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            return true;

        } catch (Exception e) {
            log.error("物流信息拉取失败: orderId={}, orderNo={}", orderId, order.getOrderNo(), e);

            // 更新同步日志为失败
            syncLog.setSyncStatus(0); // 失败
            syncLog.setErrorCode("PULL_ERROR");
            syncLog.setResponseData("拉取失败: " + e.getMessage());

            return false;

        } finally {
            // 保存同步日志
            orderSyncLogMapper.insert(syncLog);
        }
    }

    @Override
    public int batchPullLogistics(List<Long> orderIds) {
        int successCount = 0;
        for (Long orderId : orderIds) {
            if (pullLogistics(orderId)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    public int pullPendingLogistics() {
        // 查询所有已同步到ERP且需要拉取物流的订单
        // 包括：已付款未发货（状态1）和已发货（状态2）的订单
        // 状态2的订单也需要查询，因为可能物流信息有更新（如换单号等）
        List<Order> pendingOrders = orderRepository.selectList(
            new QueryWrapper<Order>()
                .eq("erp_sync_status", 1) // 已同步到ERP
                .in("order_status", Arrays.asList(1, 2))  // 已付款未发货 或 已发货
                .eq("deleted", 0)
        );

        if (pendingOrders == null || pendingOrders.isEmpty()) {
            log.info("没有待拉取物流的订单");
            return 0;
        }

        log.info("开始批量拉取物流信息，订单数量: {}", pendingOrders.size());

        List<Long> orderIds = new ArrayList<>();
        for (Order order : pendingOrders) {
            orderIds.add(order.getId());
        }

        int successCount = batchPullLogistics(orderIds);

        log.info("批量拉取物流信息完成，成功数量: {}/{}", successCount, pendingOrders.size());

        return successCount;
    }

    @Override
    public String queryLogisticsStatus(Long orderId) {
        OrderLogistics logistics = orderLogisticsMapper.selectOne(
            new QueryWrapper<OrderLogistics>().eq("order_id", orderId)
        );

        if (logistics == null) {
            return "暂无物流信息";
        }

        return String.format("物流公司: %s, 物流单号: %s",
            logistics.getLogisticsCompany(),
            logistics.getLogisticsNo()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleShipCallback(JushuitanShipCallbackDTO callbackDTO) {
        log.info("开始处理ERP发货回调: orderNo={}, logisticsCompany={}, logisticsNo={}",
                callbackDTO.getOrderNo(), callbackDTO.getLogisticsCompany(), callbackDTO.getLogisticsNo());

        // 根据订单号查询订单
        Order order = orderRepository.selectOne(
            new QueryWrapper<Order>().eq("order_no", callbackDTO.getOrderNo())
        );

        if (order == null) {
            log.error("ERP发货回调失败：订单不存在, orderNo={}", callbackDTO.getOrderNo());
            return false;
        }

        // 记录同步日志
        OrderSyncLog syncLog = new OrderSyncLog();
        syncLog.setOrderId(order.getId());
        syncLog.setOrderNo(order.getOrderNo());
        syncLog.setSyncType("SHIP_CALLBACK");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            // 记录请求数据
            syncLog.setRequestData(objectMapper.writeValueAsString(callbackDTO));

            // 检查是否已存在物流记录
            OrderLogistics existingLogistics = orderLogisticsMapper.selectOne(
                new QueryWrapper<OrderLogistics>().eq("order_id", order.getId())
            );

            LocalDateTime shipTime = null;
            // 解析发货时间
            if (callbackDTO.getShipTime() != null && !callbackDTO.getShipTime().trim().isEmpty()) {
                try {
                    shipTime = LocalDateTime.parse(callbackDTO.getShipTime(), dateTimeFormatter);
                } catch (Exception e) {
                    log.warn("解析发货时间失败: {}, 使用当前时间", callbackDTO.getShipTime(), e);
                    shipTime = LocalDateTime.now();
                }
            } else {
                shipTime = LocalDateTime.now();
            }

            if (existingLogistics == null) {
                // 新增物流记录
                OrderLogistics logistics = new OrderLogistics();
                logistics.setOrderId(order.getId());
                logistics.setLogisticsCompany(callbackDTO.getLogisticsCompany());
                logistics.setLogisticsNo(callbackDTO.getLogisticsNo());
                logistics.setShippingTime(shipTime);
                orderLogisticsMapper.insert(logistics);

                log.info("ERP发货回调：新增物流记录成功, orderId={}, logisticsNo={}", order.getId(), callbackDTO.getLogisticsNo());
            } else {
                // 更新物流记录
                existingLogistics.setLogisticsCompany(callbackDTO.getLogisticsCompany());
                existingLogistics.setLogisticsNo(callbackDTO.getLogisticsNo());
                existingLogistics.setShippingTime(shipTime);
                orderLogisticsMapper.updateById(existingLogistics);

                log.info("ERP发货回调：更新物流记录成功, orderId={}, logisticsNo={}", order.getId(), callbackDTO.getLogisticsNo());
            }

            // 更新订单状态为已发货（如果当前状态是已付款未发货）
            if (order.getOrderStatus() == 1) { // 已付款未发货
                order.setOrderStatus(2); // 已发货
                order.setShipTime(shipTime);
                orderRepository.updateById(order);
                log.info("ERP发货回调：订单状态已更新为已发货, orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            } else if (order.getOrderStatus() == 2) {
                // 如果已经是已发货状态，只更新发货时间
                order.setShipTime(shipTime);
                orderRepository.updateById(order);
                log.info("ERP发货回调：订单已是已发货状态，仅更新发货时间, orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            } else {
                log.warn("ERP发货回调：订单状态不是已付款未发货，跳过状态更新, orderId={}, orderNo={}, orderStatus={}",
                        order.getId(), order.getOrderNo(), order.getOrderStatus());
            }

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            syncLog.setResponseData("ERP发货回调处理成功");
            orderSyncLogMapper.insert(syncLog);

            log.info("ERP发货回调处理成功: orderId={}, orderNo={}, logisticsNo={}", order.getId(), order.getOrderNo(), callbackDTO.getLogisticsNo());
            return true;

        } catch (Exception e) {
            log.error("ERP发货回调处理失败: orderId={}, orderNo={}", order.getId(), order.getOrderNo(), e);

            // 更新同步日志为失败
            syncLog.setSyncStatus(0); // 失败
            syncLog.setErrorCode("CALLBACK_ERROR");
            syncLog.setResponseData("回调处理失败: " + e.getMessage());
            orderSyncLogMapper.insert(syncLog);

            return false;
        }
    }
}
