package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
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

    @Resource
    private com.shoppingmall.service.erp.JushuitanConfigService jushuitanConfigService;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL);
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

        // 获取当前ERP配置的环境类型和店铺ID
        String envType = "production"; // 默认生产环境
        String shopId = null;
        try {
            com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanApiService.isEnabled()
                ? jushuitanConfigService.getEnabledConfig()
                : null;
            if (currentConfig != null) {
                if (currentConfig.getEnvType() != null) {
                    envType = currentConfig.getEnvType();
                }
                shopId = currentConfig.getShopId(); // 获取店铺ID
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
        syncLog.setSyncType("PUSH_ORDER");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            // 转换为聚水潭订单DTO
            JushuitanOrderDTO orderDTO = convertToJushuitanOrder(order, orderItems);
            // 设置店铺ID（从配置中获取）
            orderDTO.setShopId(shopId);

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
        // 查询订单信息
        Order order = orderRepository.selectById(orderId);
        if (order == null) {
            log.error("订单不存在: orderId={}", orderId);
            return false;
        }

        // 查找该订单最新的失败记录
        List<OrderSyncLog> logs = orderSyncLogMapper.selectList(
            new QueryWrapper<OrderSyncLog>()
                .eq("order_id", orderId)
                .eq("sync_type", "PUSH_ORDER")
                .eq("sync_status", 0) // 失败状态
                .orderByDesc("create_time")
                .last("LIMIT 1")
        );

        OrderSyncLog syncLog;
        if (logs != null && !logs.isEmpty()) {
            // 使用现有的失败记录
            syncLog = logs.get(0);
            syncLog.setRetryCount(syncLog.getRetryCount() + 1);
            syncLog.setSyncStatus(2); // 更新为处理中
            orderSyncLogMapper.updateById(syncLog);
        } else {
            // 如果没有失败记录，创建新记录（首次推送）
            return pushOrder(orderId);
        }

        // 查询订单商品
        List<OrderItem> orderItems = orderItemRepository.selectList(
            new QueryWrapper<OrderItem>().eq("order_id", orderId)
        );

        try {
            // 转换为聚水潭订单DTO
            JushuitanOrderDTO orderDTO = convertToJushuitanOrder(order, orderItems);

            // 获取并设置店铺ID
            try {
                com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
                if (currentConfig != null && currentConfig.getShopId() != null) {
                    String shopId = currentConfig.getShopId();
                    // 空字符串转为 null，避免发送空值给聚水潭API
                    if (shopId.trim().isEmpty()) {
                        shopId = null;
                    }
                    orderDTO.setShopId(shopId);
                }
            } catch (Exception e) {
                log.warn("获取店铺ID失败", e);
            }

            // 更新请求数据
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
            orderSyncLogMapper.updateById(syncLog);

            log.info("订单重试推送成功: orderId={}, orderNo={}, erpOrderId={}, retryCount={}",
                orderId, order.getOrderNo(), erpOrderId, syncLog.getRetryCount());
            return true;

        } catch (Exception e) {
            log.error("订单重试推送失败: orderId={}, orderNo={}, retryCount={}",
                orderId, order.getOrderNo(), syncLog.getRetryCount(), e);

            // 更新订单ERP状态为失败
            order.setErpSyncStatus(2); // 同步失败
            orderRepository.updateById(order);

            // 更新同步日志为失败
            syncLog.setSyncStatus(0); // 失败
            syncLog.setErrorCode("PUSH_ERROR");
            syncLog.setResponseData("推送失败: " + e.getMessage());
            orderSyncLogMapper.updateById(syncLog);

            return false;
        }
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

    @Override
    public com.shoppingmall.vo.OrderPushResultVO pushOrderWithDetail(Long orderId) {
        com.shoppingmall.vo.OrderPushResultVO result = new com.shoppingmall.vo.OrderPushResultVO();
        result.setOrderId(orderId);
        result.setSuccess(false);

        StringBuilder logBuilder = new StringBuilder();

        try {
            logBuilder.append("===== 订单推送详细日志 =====\n\n");

            // 检查ERP是否启用
            if (!jushuitanApiService.isEnabled()) {
                logBuilder.append("[错误] 聚水潭配置未启用\n");
                result.setErrorMessage("聚水潭配置未启用");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            // 查询订单信息
            Order order = orderRepository.selectById(orderId);
            if (order == null) {
                logBuilder.append("[错误] 订单不存在: orderId=").append(orderId).append("\n");
                result.setErrorMessage("订单不存在");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            result.setOrderNo(order.getOrderNo());
            logBuilder.append("[信息] 订单号: ").append(order.getOrderNo()).append("\n");

            // 检查订单是否已支付
            if (order.getPaymentStatus() != 2) {
                logBuilder.append("[错误] 订单未支付，无法推送\n");
                logBuilder.append("       支付状态: ").append(order.getPaymentStatus()).append("\n");
                result.setErrorMessage("订单未支付");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            logBuilder.append("[信息] 订单已支付，准备推送\n\n");

            // 查询订单商品
            List<OrderItem> orderItems = orderItemRepository.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId)
            );

            if (orderItems == null || orderItems.isEmpty()) {
                logBuilder.append("[错误] 订单商品为空\n");
                result.setErrorMessage("订单商品为空");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            logBuilder.append("[信息] 订单商品数量: ").append(orderItems.size()).append("\n\n");

            // 获取配置
            String shopId = null;
            try {
                com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
                if (currentConfig != null) {
                    shopId = currentConfig.getShopId();
                    logBuilder.append("[配置] API地址: ").append(currentConfig.getApiUrl()).append("\n");
                    logBuilder.append("[配置] App Key: ").append(currentConfig.getAppKey()).append("\n");
                    logBuilder.append("[配置] 店铺ID: ").append(shopId != null ? shopId : "未设置").append("\n\n");
                }
            } catch (Exception e) {
                logBuilder.append("[警告] 获取配置失败: ").append(e.getMessage()).append("\n\n");
            }

            // 转换订单数据
            JushuitanOrderDTO orderDTO = convertToJushuitanOrder(order, orderItems);
            orderDTO.setShopId(shopId);

            String requestJson = objectMapper.writeValueAsString(orderDTO);
            result.setRequestData(requestJson);

            logBuilder.append("[请求] 订单数据:\n");
            logBuilder.append(formatJson(requestJson)).append("\n\n");

            // 推送订单
            logBuilder.append("[推送] 开始调用聚水潭API...\n");
            String erpOrderId = jushuitanApiService.uploadOrder(orderDTO);

            result.setErpOrderId(erpOrderId);
            logBuilder.append("[成功] ERP订单ID: ").append(erpOrderId).append("\n");

            // 更新订单状态
            order.setErpSyncStatus(1);
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(erpOrderId);
            orderRepository.updateById(order);

            logBuilder.append("[信息] 订单状态已更新为'已同步'\n");
            result.setSuccess(true);
            result.setResponseData("订单推送成功");

        } catch (Exception e) {
            logBuilder.append("\n[异常] 推送失败:\n");
            logBuilder.append("       异常类型: ").append(e.getClass().getSimpleName()).append("\n");
            logBuilder.append("       错误信息: ").append(e.getMessage()).append("\n");
            result.setErrorMessage(e.getMessage());
            result.setResponseData("推送失败: " + e.getMessage());
        }

        result.setPushLog(logBuilder.toString());
        return result;
    }

    @Override
    public com.shoppingmall.vo.OrderPushResultVO retryPushOrderWithDetail(Long orderId) {
        com.shoppingmall.vo.OrderPushResultVO result = new com.shoppingmall.vo.OrderPushResultVO();
        result.setOrderId(orderId);
        result.setSuccess(false);

        StringBuilder logBuilder = new StringBuilder();
        OrderSyncLog syncLog = null;
        Order order = null;

        try {
            logBuilder.append("===== 重试推送订单详细日志 =====\n\n");

            // 查询订单信息
            order = orderRepository.selectById(orderId);
            if (order == null) {
                logBuilder.append("[错误] 订单不存在: orderId=").append(orderId).append("\n");
                result.setErrorMessage("订单不存在");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            result.setOrderNo(order.getOrderNo());
            logBuilder.append("[信息] 订单号: ").append(order.getOrderNo()).append("\n\n");

            // 查找失败记录并更新
            List<OrderSyncLog> logs = orderSyncLogMapper.selectList(
                new QueryWrapper<OrderSyncLog>()
                    .eq("order_id", orderId)
                    .eq("sync_type", "PUSH_ORDER")
                    .eq("sync_status", 0)
                    .orderByDesc("create_time")
                    .last("LIMIT 1")
            );

            if (logs != null && !logs.isEmpty()) {
                // 使用现有失败记录，更新重试次数
                syncLog = logs.get(0);
                syncLog.setRetryCount(syncLog.getRetryCount() + 1);  // 增加重试次数
                syncLog.setSyncStatus(2); // 设置为处理中
                orderSyncLogMapper.updateById(syncLog);

                logBuilder.append("[信息] 找到失败记录，重试次数: ").append(syncLog.getRetryCount()).append("\n");
                logBuilder.append("[信息] 上次失败原因: ").append(syncLog.getResponseData()).append("\n\n");
            } else {
                // 未找到失败记录，创建新记录（首次推送）
                logBuilder.append("[信息] 未找到失败记录，创建新的同步日志\n\n");

                // 获取环境类型
                String envType = "production";
                try {
                    com.shoppingmall.vo.JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
                    if (config != null && config.getEnvType() != null) {
                        envType = config.getEnvType();
                    }
                } catch (Exception e) {
                    // ignore
                }

                syncLog = new OrderSyncLog();
                syncLog.setOrderId(orderId);
                syncLog.setOrderNo(order.getOrderNo());
                syncLog.setEnvType(envType);
                syncLog.setSyncType("PUSH_ORDER");
                syncLog.setSyncStatus(2); // 处理中
                syncLog.setRetryCount(0);
                orderSyncLogMapper.insert(syncLog);
            }

            // 查询订单商品
            List<OrderItem> orderItems = orderItemRepository.selectList(
                new QueryWrapper<OrderItem>().eq("order_id", orderId)
            );

            if (orderItems == null || orderItems.isEmpty()) {
                logBuilder.append("[错误] 订单商品为空\n");
                result.setErrorMessage("订单商品为空");
                result.setPushLog(logBuilder.toString());
                return result;
            }

            // 获取配置并转换订单数据
            JushuitanOrderDTO orderDTO = convertToJushuitanOrder(order, orderItems);

            String shopId = null;
            try {
                com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
                if (currentConfig != null) {
                    shopId = currentConfig.getShopId();
                    // 空字符串转为 null，避免发送空值给聚水潭API
                    if (shopId != null && shopId.trim().isEmpty()) {
                        shopId = null;
                    }
                    orderDTO.setShopId(shopId);
                    logBuilder.append("[配置] API地址: ").append(currentConfig.getApiUrl()).append("\n");
                    logBuilder.append("[配置] App Key: ").append(currentConfig.getAppKey()).append("\n");
                    logBuilder.append("[配置] 店铺ID: ").append(shopId != null ? shopId : "未设置").append("\n\n");
                }
            } catch (Exception e) {
                logBuilder.append("[警告] 获取配置失败: ").append(e.getMessage()).append("\n\n");
            }

            String requestJson = objectMapper.writeValueAsString(orderDTO);
            result.setRequestData(requestJson);

            // 更新同步日志的请求数据
            syncLog.setRequestData(requestJson);

            logBuilder.append("[请求] 订单数据:\n");
            logBuilder.append(formatJson(requestJson)).append("\n\n");

            // 推送订单
            logBuilder.append("[推送] 开始重试调用聚水潭API...\n");
            String erpOrderId = jushuitanApiService.uploadOrder(orderDTO);

            result.setErpOrderId(erpOrderId);
            logBuilder.append("[成功] ERP订单ID: ").append(erpOrderId).append("\n");

            // 更新订单状态
            order.setErpSyncStatus(1);
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(erpOrderId);
            orderRepository.updateById(order);

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            syncLog.setResponseData("订单推送成功，ERP订单ID: " + erpOrderId);
            syncLog.setErrorCode(null);
            orderSyncLogMapper.updateById(syncLog);

            logBuilder.append("[信息] 订单状态已更新为'已同步'\n");
            logBuilder.append("[信息] 同步日志已更新为成功状态\n");
            result.setSuccess(true);
            result.setResponseData("订单重试推送成功");

        } catch (Exception e) {
            logBuilder.append("\n[异常] 重试推送失败:\n");
            logBuilder.append("       异常类型: ").append(e.getClass().getSimpleName()).append("\n");
            logBuilder.append("       错误信息: ").append(e.getMessage()).append("\n");
            result.setErrorMessage(e.getMessage());
            result.setResponseData("重试失败: " + e.getMessage());

            // 更新订单ERP状态为失败
            if (order != null) {
                order.setErpSyncStatus(2);
                orderRepository.updateById(order);
            }

            // 更新同步日志为失败
            if (syncLog != null) {
                syncLog.setSyncStatus(0); // 失败
                syncLog.setErrorCode("PUSH_ERROR");
                syncLog.setResponseData("推送失败: " + e.getMessage());
                orderSyncLogMapper.updateById(syncLog);

                logBuilder.append("[信息] 同步日志已更新为失败状态，重试次数: ").append(syncLog.getRetryCount()).append("\n");
            }
        }

        result.setPushLog(logBuilder.toString());
        return result;
    }

    /**
     * 格式化JSON字符串
     */
    private String formatJson(String json) {
        try {
            Object obj = objectMapper.readValue(json, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return json;
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
        dto.setPayAmount(order.getActualAmount()); // 设置支付金额

        // 解析收货地址
        if (order.getShippingAddress() != null && !order.getShippingAddress().isEmpty()) {
            JsonNode addressNode = objectMapper.readTree(order.getShippingAddress());

            // 收货人姓名：优先使用 name，其次 receiverName
            if (addressNode.has("name")) {
                dto.setReceiverName(addressNode.get("name").asText());
            } else if (addressNode.has("receiverName")) {
                dto.setReceiverName(addressNode.get("receiverName").asText());
            }

            // 收货人手机：优先使用 mobile，其次 phone，最后 receiverMobile
            if (addressNode.has("mobile")) {
                dto.setReceiverMobile(addressNode.get("mobile").asText());
            } else if (addressNode.has("phone")) {
                dto.setReceiverMobile(addressNode.get("phone").asText());
            } else if (addressNode.has("receiverMobile")) {
                dto.setReceiverMobile(addressNode.get("receiverMobile").asText());
            }

            if (addressNode.has("province")) {
                dto.setReceiverProvince(addressNode.get("province").asText());
            }
            if (addressNode.has("city")) {
                dto.setReceiverCity(addressNode.get("city").asText());
            }
            if (addressNode.has("district")) {
                dto.setReceiverDistrict(addressNode.get("district").asText());
            }

            // 详细地址：组合 district + address（或 detailAddress）
            StringBuilder addressBuilder = new StringBuilder();
            if (addressNode.has("district")) {
                addressBuilder.append(addressNode.get("district").asText());
            }
            if (addressNode.has("address")) {
                addressBuilder.append(addressNode.get("address").asText());
            } else if (addressNode.has("detailAddress")) {
                addressBuilder.append(addressNode.get("detailAddress").asText());
            }
            if (addressBuilder.length() > 0) {
                dto.setReceiverAddress(addressBuilder.toString());
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

        // 构建支付信息（必填）
        List<JushuitanOrderDTO.Pay> payList = new ArrayList<>();
        JushuitanOrderDTO.Pay payInfo = new JushuitanOrderDTO.Pay();

        // 支付单号（使用订单号）
        payInfo.setOuterPayId(order.getOrderNo());

        // 支付时间
        if (order.getPayTime() != null) {
            payInfo.setPayDate(order.getPayTime().format(dateTimeFormatter));
        }

        // 支付方式（根据订单支付方式映射）
        if (order.getPaymentMethod() != null) {
            payInfo.setPayment(order.getPaymentMethod());
        } else {
            payInfo.setPayment("在线支付");
        }

        // 实付金额（必填，必须等于 pay_amount）
        payInfo.setAmount(order.getActualAmount());

        payList.add(payInfo);
        dto.setPay(payList);

        return dto;
    }
}
