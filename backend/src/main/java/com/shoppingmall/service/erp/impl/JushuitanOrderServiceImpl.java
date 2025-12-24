package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.OrderSyncLog;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.mapper.OrderSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanApiService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 聚水潭订单推送服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
public class JushuitanOrderServiceImpl implements JushuitanOrderService {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private OrderItemRepository orderItemRepository;

    @Resource
    private OrderSyncLogMapper orderSyncLogMapper;

    @Resource
    private JushuitanApiService jushuitanApiService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean pushOrder(Long orderId) {
        // 检查ERP是否启用
        if (!jushuitanApiService.isEnabled()) {
            log.warn("聚水潭配置未启用，跳过订单推送: orderId={}", orderId);
            return false;
        }

        // 查询订单信息
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            return false;
        }

        // 检查订单是否已支付
        if (order.getPaymentStatus() != 2) {
            log.warn("订单未支付，无法推送: orderId={}, paymentStatus={}", orderId, order.getPaymentStatus());
            return false;
        }

        // 查询订单商品
        List<OrderItem> orderItems = orderItemRepository.selectList(
            new QueryWrapper<OrderItem>().eq("order_id", orderId)
        );

        if (orderItems == null || orderItems.isEmpty()) {
            log.error("订单商品为空: orderId={}", orderId);
            return false;
        }

        // 记录同步日志
        OrderSyncLog syncLog = new OrderSyncLog();
        syncLog.setOrderId(orderId);
        syncLog.setOrderNo(order.getOrderNo());
        syncLog.setSyncType("PUSH_ORDER");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            // 转换为聚水潭订单DTO
            JushuitanOrderDTO orderDTO = convertToJushuitanOrder(order, orderItems);

            // 记录请求数据
            syncLog.setRequestData(objectMapper.writeValueAsString(orderDTO));

            // 推送订单到聚水潭
            String erpOrderId = jushuitanApiService.uploadOrder(orderDTO);

            // 更新订单ERP状态
            order.setErpSyncStatus(1); // 已同步
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(erpOrderId);
            orderRepository.updateById(order);

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            syncLog.setResponseData("订单推送成功，ERP订单ID: " + erpOrderId);

            log.info("订单推送成功: orderId={}, orderNo={}, erpOrderId={}", orderId, order.getOrderNo(), erpOrderId);
            return true;

        } catch (Exception e) {
            log.error("订单推送失败: orderId={}, orderNo={}", orderId, order.getOrderNo(), e);

            // 更新订单ERP状态为失败
            order.setErpSyncStatus(2); // 同步失败
            orderRepository.updateById(order);

            // 更新同步日志为失败
            syncLog.setSyncStatus(0); // 失败
            syncLog.setErrorCode("PUSH_ERROR");
            syncLog.setResponseData("推送失败: " + e.getMessage());

            return false;

        } finally {
            // 保存同步日志
            orderSyncLogMapper.insert(syncLog);
        }
    }

    @Override
    public int batchPushOrders(List<Long> orderIds) {
        int successCount = 0;
        for (Long orderId : orderIds) {
            if (pushOrder(orderId)) {
                successCount++;
            }
        }
        return successCount;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryPushOrder(Long orderId) {
        // 查询最后一次同步日志
        OrderSyncLog lastLog = orderSyncLogMapper.selectOne(
            new QueryWrapper<OrderSyncLog>()
                .eq("order_id", orderId)
                .eq("sync_type", "PUSH_ORDER")
                .orderByDesc("create_time")
                .last("LIMIT 1")
        );

        if (lastLog != null) {
            // 更新重试次数
            OrderSyncLog newLog = new OrderSyncLog();
            newLog.setOrderId(orderId);
            newLog.setOrderNo(lastLog.getOrderNo());
            newLog.setSyncType("PUSH_ORDER");
            newLog.setRetryCount(lastLog.getRetryCount() + 1);
            orderSyncLogMapper.insert(newLog);
        }

        // 执行推送
        return pushOrder(orderId);
    }

    @Override
    public String queryPushStatus(Long orderId) {
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            return "订单不存在";
        }

        Integer syncStatus = order.getErpSyncStatus();
        if (syncStatus == null || syncStatus == 0) {
            return "未同步";
        } else if (syncStatus == 1) {
            return "已同步 (ERP订单ID: " + order.getErpOrderId() + ")";
        } else {
            return "同步失败";
        }
    }

    /**
     * 转换订单为聚水潭订单DTO
     */
    private JushuitanOrderDTO convertToJushuitanOrder(Order order, List<OrderItem> orderItems) throws Exception {
        JushuitanOrderDTO dto = new JushuitanOrderDTO();

        // 基本信息
        dto.setSoId(order.getOrderNo());
        dto.setOrderDate(order.getCreateTime().format(dateTimeFormatter));
        dto.setPayDate(order.getPayTime() != null ? order.getPayTime().format(dateTimeFormatter) : null);
        dto.setFreight(order.getShippingFee());

        // 解析收货地址
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            JsonNode addressNode = objectMapper.readTree(order.getShippingAddress());

            if (addressNode.has("receiverName")) {
                dto.setReceiverName(addressNode.get("receiverName").asText());
            }
            if (addressNode.has("receiverMobile")) {
                dto.setReceiverMobile(addressNode.get("receiverMobile").asText());
            }
            if (addressNode.has("province")) {
                dto.setReceiverProvince(addressNode.get("province").asText());
            }
            if (addressNode.has("city")) {
                dto.setReceiverCity(addressNode.get("city").asText());
            }
            if (addressNode.has("district")) {
                // 组合详细地址
                String district = addressNode.get("district").asText();
                String detailAddress = addressNode.has("detailAddress") ?
                    addressNode.get("detailAddress").asText() : "";
                dto.setReceiverAddress(district + detailAddress);
            }
        }

        // 转换订单商品
        List<JushuitanOrderDTO.Item> items = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            JushuitanOrderDTO.Item item = new JushuitanOrderDTO.Item();

            // 使用SKU ID或商品编码
            if (orderItem.getSkuId() != null) {
                item.setSkuId(String.valueOf(orderItem.getSkuId()));
            } else if (orderItem.getProductCode() != null) {
                item.setSkuId(orderItem.getProductCode());
            } else {
                item.setSkuId(String.valueOf(orderItem.getProductId()));
            }

            item.setItemName(orderItem.getProductName());
            item.setQty(orderItem.getQuantity());
            item.setPrice(orderItem.getPrice());

            items.add(item);
        }
        dto.setItems(items);

        return dto;
    }
}
