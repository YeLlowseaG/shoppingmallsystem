package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanOrderDTO;
import com.shoppingmall.dto.JushuitanUploadOrderResponseDTO;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.OrderSyncLog;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.mapper.OrderSyncLogMapper;
import com.shoppingmall.service.erp.JushuitanApiService;
import com.shoppingmall.service.erp.JushuitanOrderService;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.entity.User;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    @Resource
    private UserRepository userRepository;

    @Resource
    private ProductRepository productRepository;

    @Resource
    private ProductSkuRepository productSkuRepository;

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
            // 设置店铺ID（从配置中获取，转换为Integer）
            if (shopId != null && !shopId.trim().isEmpty()) {
                try {
                    orderDTO.setShopId(Integer.parseInt(shopId));
                } catch (NumberFormatException e) {
                    log.warn("shop_id格式错误，无法转换为Integer: {}", shopId);
                }
            }
            // 设置店铺买家ID：获取下单用户的姓名
            String buyerName = getBuyerName(order.getUserId());
            orderDTO.setShopBuyerId(buyerName != null ? buyerName : (order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo()));
            
            // 设置订单状态：固定传WAIT_SELLER_SEND_GOODS（等待卖家发货）
            orderDTO.setShopStatus("WAIT_SELLER_SEND_GOODS");

            // 记录请求数据
            syncLog.setRequestData(objectMapper.writeValueAsString(orderDTO));

            // 推送订单到聚水潭
            JushuitanUploadOrderResponseDTO responseDTO = jushuitanApiService.uploadOrder(orderDTO);

            // 保存完整响应到同步日志
            syncLog.setResponseData(responseDTO.getResponseJson());

            // 更新订单ERP状态
            order.setErpSyncStatus(1); // 已同步
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(responseDTO.getErpOrderId());
            // 保存ERP内部订单号（o_id）
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                order.setErpInternalOrderId(responseDTO.getErpInternalOrderId());
            }
            orderRepository.updateById(order);

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功

            log.info("订单推送成功: orderId={}, orderNo={}, erpOrderId={}, erpInternalOrderId={}", 
                orderId, order.getOrderNo(), responseDTO.getErpOrderId(), responseDTO.getErpInternalOrderId());
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

            // 获取并设置店铺ID（转换为Integer）
            try {
                com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
                if (currentConfig != null && currentConfig.getShopId() != null) {
                    String shopIdStr = currentConfig.getShopId();
                    // 空字符串转为 null，避免发送空值给聚水潭API
                    if (!shopIdStr.trim().isEmpty()) {
                        try {
                            orderDTO.setShopId(Integer.parseInt(shopIdStr));
                        } catch (NumberFormatException e) {
                            log.warn("shop_id格式错误，无法转换为Integer: {}", shopIdStr);
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取店铺ID失败", e);
            }
            // 设置店铺买家ID：获取下单用户的姓名
            String buyerName = getBuyerName(order.getUserId());
            orderDTO.setShopBuyerId(buyerName != null ? buyerName : (order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo()));
            
            // 设置订单状态：固定传WAIT_SELLER_SEND_GOODS（等待卖家发货）
            orderDTO.setShopStatus("WAIT_SELLER_SEND_GOODS");

            // 更新请求数据
            syncLog.setRequestData(objectMapper.writeValueAsString(orderDTO));

            // 推送订单到聚水潭
            JushuitanUploadOrderResponseDTO responseDTO = jushuitanApiService.uploadOrder(orderDTO);

            // 保存完整响应到同步日志
            syncLog.setResponseData(responseDTO.getResponseJson());

            // 更新订单ERP状态
            order.setErpSyncStatus(1); // 已同步
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(responseDTO.getErpOrderId());
            // 保存ERP内部订单号（o_id）
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                order.setErpInternalOrderId(responseDTO.getErpInternalOrderId());
            }
            orderRepository.updateById(order);

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            orderSyncLogMapper.updateById(syncLog);

            log.info("订单重试推送成功: orderId={}, orderNo={}, erpOrderId={}, erpInternalOrderId={}, retryCount={}",
                orderId, order.getOrderNo(), responseDTO.getErpOrderId(), responseDTO.getErpInternalOrderId(), syncLog.getRetryCount());
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
            // 设置店铺ID（转换为Integer）
            if (shopId != null && !shopId.trim().isEmpty()) {
                try {
                    orderDTO.setShopId(Integer.parseInt(shopId));
                } catch (NumberFormatException e) {
                    logBuilder.append("[警告] shop_id格式错误，无法转换为Integer: ").append(shopId).append("\n");
                }
            }
            // 设置店铺买家ID：获取下单用户的姓名
            String buyerName = getBuyerName(order.getUserId());
            orderDTO.setShopBuyerId(buyerName != null ? buyerName : (order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo()));
            
            // 设置订单状态：固定传WAIT_SELLER_SEND_GOODS（等待卖家发货）
            orderDTO.setShopStatus("WAIT_SELLER_SEND_GOODS");

            String requestJson = objectMapper.writeValueAsString(orderDTO);
            result.setRequestData(requestJson);

            logBuilder.append("[请求] 订单数据:\n");
            logBuilder.append(formatJson(requestJson)).append("\n\n");

            // 推送订单
            logBuilder.append("[推送] 开始调用聚水潭API...\n");
            JushuitanUploadOrderResponseDTO responseDTO = jushuitanApiService.uploadOrder(orderDTO);

            result.setErpOrderId(responseDTO.getErpOrderId());
            logBuilder.append("[成功] ERP订单ID: ").append(responseDTO.getErpOrderId()).append("\n");
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                logBuilder.append("[成功] ERP内部订单号: ").append(responseDTO.getErpInternalOrderId()).append("\n");
            }

            // 更新订单状态
            order.setErpSyncStatus(1);
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(responseDTO.getErpOrderId());
            // 保存ERP内部订单号（o_id）
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                order.setErpInternalOrderId(responseDTO.getErpInternalOrderId());
            }
            orderRepository.updateById(order);

            logBuilder.append("[信息] 订单状态已更新为'已同步'\n");
            result.setSuccess(true);
            result.setResponseData(responseDTO.getResponseJson() != null ? responseDTO.getResponseJson() : "订单推送成功");

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

            String shopIdStr = null;
            try {
                com.shoppingmall.vo.JushuitanConfigVO currentConfig = jushuitanConfigService.getEnabledConfig();
                if (currentConfig != null) {
                    shopIdStr = currentConfig.getShopId();
                    // 空字符串转为 null，避免发送空值给聚水潭API
                    if (shopIdStr != null && !shopIdStr.trim().isEmpty()) {
                        try {
                            orderDTO.setShopId(Integer.parseInt(shopIdStr));
                        } catch (NumberFormatException e) {
                            logBuilder.append("[警告] shop_id格式错误，无法转换为Integer: ").append(shopIdStr).append("\n");
                        }
                    }
                    logBuilder.append("[配置] API地址: ").append(currentConfig.getApiUrl()).append("\n");
                    logBuilder.append("[配置] App Key: ").append(currentConfig.getAppKey()).append("\n");
                    logBuilder.append("[配置] 店铺ID: ").append(shopIdStr != null ? shopIdStr : "未设置").append("\n\n");
                }
            } catch (Exception e) {
                logBuilder.append("[警告] 获取配置失败: ").append(e.getMessage()).append("\n\n");
            }
            // 设置店铺买家ID：获取下单用户的姓名
            String buyerName = getBuyerName(order.getUserId());
            orderDTO.setShopBuyerId(buyerName != null ? buyerName : (order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo()));
            
            // 设置订单状态：固定传WAIT_SELLER_SEND_GOODS（等待卖家发货）
            orderDTO.setShopStatus("WAIT_SELLER_SEND_GOODS");

            String requestJson = objectMapper.writeValueAsString(orderDTO);
            result.setRequestData(requestJson);

            // 更新同步日志的请求数据
            syncLog.setRequestData(requestJson);

            logBuilder.append("[请求] 订单数据:\n");
            logBuilder.append(formatJson(requestJson)).append("\n\n");

            // 推送订单
            logBuilder.append("[推送] 开始重试调用聚水潭API...\n");
            JushuitanUploadOrderResponseDTO responseDTO = jushuitanApiService.uploadOrder(orderDTO);

            result.setErpOrderId(responseDTO.getErpOrderId());
            logBuilder.append("[成功] ERP订单ID: ").append(responseDTO.getErpOrderId()).append("\n");
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                logBuilder.append("[成功] ERP内部订单号: ").append(responseDTO.getErpInternalOrderId()).append("\n");
            }

            // 更新订单状态
            order.setErpSyncStatus(1);
            order.setErpSyncTime(LocalDateTime.now());
            order.setErpOrderId(responseDTO.getErpOrderId());
            // 保存ERP内部订单号（o_id）
            if (responseDTO.getErpInternalOrderId() != null && !responseDTO.getErpInternalOrderId().isEmpty()) {
                order.setErpInternalOrderId(responseDTO.getErpInternalOrderId());
            }
            orderRepository.updateById(order);

            // 更新同步日志为成功
            syncLog.setSyncStatus(1); // 成功
            syncLog.setResponseData(responseDTO.getResponseJson());
            syncLog.setErrorCode(null);
            orderSyncLogMapper.updateById(syncLog);

            logBuilder.append("[信息] 订单状态已更新为'已同步'\n");
            logBuilder.append("[信息] 同步日志已更新为成功状态\n");
            result.setSuccess(true);
            result.setResponseData(responseDTO.getResponseJson() != null ? responseDTO.getResponseJson() : "订单重试推送成功");

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
    public JushuitanOrderDTO convertToJushuitanOrder(Order order, List<OrderItem> orderItems) throws Exception {
        JushuitanOrderDTO dto = new JushuitanOrderDTO();

        // 基本信息
        dto.setSoId(order.getOrderNo());
        dto.setOrderDate(order.getCreateTime().format(dateTimeFormatter));
        dto.setPayDate(order.getPayTime() != null ? order.getPayTime().format(dateTimeFormatter) : null);
        dto.setFreight(order.getShippingFee());
        dto.setPayAmount(order.getActualAmount()); // 设置支付金额
        
        // 设置订单状态：固定传WAIT_SELLER_SEND_GOODS（等待卖家发货）
        dto.setShopStatus("WAIT_SELLER_SEND_GOODS");
        
        // 设置店铺买家ID：获取下单用户的姓名
        String buyerName = getBuyerName(order.getUserId());
        dto.setShopBuyerId(buyerName != null ? buyerName : (order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo()));

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

            // 收货人电话：使用phone字段
            if (addressNode.has("phone")) {
                dto.setReceiverPhone(addressNode.get("phone").asText());
            }

            // 邮政编码：使用zipCode字段
            if (addressNode.has("zipCode")) {
                dto.setReceiverZip(addressNode.get("zipCode").asText());
            }

            if (addressNode.has("province")) {
                dto.setReceiverState(addressNode.get("province").asText());
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

        // buyer_message字段：传订单附言的字段内容（orderRemark）
        if (order.getOrderRemark() != null && !order.getOrderRemark().isEmpty()) {
            dto.setBuyerMessage(order.getOrderRemark());
        }

        // 转换订单商品
        List<JushuitanOrderDTO.Item> items = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            JushuitanOrderDTO.Item item = new JushuitanOrderDTO.Item();

            // sku_id字段：需要和普通商品资料上传接口的sku_id逻辑对应
            // 如果订单项有SKU ID，查询SKU信息获取SKU编码；否则使用商品编码
            String skuIdStr = null;
            String shopIId = orderItem.getProductCode(); // shop_i_id：传商品编码
            
            if (orderItem.getSkuId() != null) {
                // 有SKU ID，查询SKU信息
                ProductSku sku = productSkuRepository.selectById(orderItem.getSkuId());
                if (sku != null && sku.getSkuCode() != null && !sku.getSkuCode().isEmpty()) {
                    // 使用SKU编码
                    skuIdStr = sku.getSkuCode();
                } else {
                    // SKU没有编码，使用商品编码+SKU ID
                    Product product = productRepository.selectById(orderItem.getProductId());
                    if (product != null && product.getProductCode() != null) {
                        skuIdStr = product.getProductCode() + "_" + orderItem.getSkuId();
                    } else {
                        skuIdStr = String.valueOf(orderItem.getSkuId());
                    }
                }
                
                // properties_value字段：按照"规格属性+规格值"的方式拼接
                if (sku != null && sku.getSpecCombination() != null && !sku.getSpecCombination().isEmpty()) {
                    try {
                        // 解析specCombination JSON字符串，格式如：{"颜色":"蓝色","尺码":"XXL"}
                        Map<String, String> specMap = objectMapper.readValue(
                            sku.getSpecCombination(), 
                            new TypeReference<Map<String, String>>() {}
                        );
                        
                        // 按照"规格属性+规格值"格式拼接，例如"颜色:蓝色;尺码:XXL"
                        List<String> specParts = new ArrayList<>();
                        for (Map.Entry<String, String> entry : specMap.entrySet()) {
                            specParts.add(entry.getKey() + ":" + entry.getValue());
                        }
                        
                        if (!specParts.isEmpty()) {
                            String propertiesValue = String.join(";", specParts);
                            item.setPropertiesValue(propertiesValue);
                        }
                    } catch (Exception e) {
                        log.warn("解析订单项SKU规格组合失败: orderItemId={}, specCombination={}", 
                                orderItem.getId(), sku.getSpecCombination(), e);
                    }
                }
            } else {
                // 没有SKU ID，使用商品编码
                if (orderItem.getProductCode() != null) {
                    skuIdStr = orderItem.getProductCode();
                } else {
                    Product product = productRepository.selectById(orderItem.getProductId());
                    if (product != null && product.getProductCode() != null) {
                        skuIdStr = product.getProductCode();
                    } else {
                        skuIdStr = String.valueOf(orderItem.getProductId());
                    }
                }
            }

            item.setSkuId(skuIdStr);
            item.setShopSkuId(skuIdStr); // 店铺SKU ID，通常与sku_id相同
            item.setShopIId(shopIId); // shop_i_id：传商品编码
            item.setName(orderItem.getProductName()); // 使用name字段（必填）
            // item_name字段不需要，不设置（已从DTO中移除）
            item.setQty(orderItem.getQuantity());
            item.setPrice(orderItem.getPrice());
            item.setBasePrice(orderItem.getPrice()); // 基础价格，通常与price相同
            // 金额 = 单价 * 数量
            BigDecimal amount = orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
            item.setAmount(amount);
            item.setOuterOiId(String.valueOf(orderItem.getId())); // 使用订单项ID作为外部订单项ID（必填）

            items.add(item);
        }
        dto.setItems(items);

        // 构建支付信息（必填）- 注意：pay是对象，不是数组
        JushuitanOrderDTO.Pay payInfo = new JushuitanOrderDTO.Pay();

        // 支付单号（使用订单号）
        payInfo.setOuterPayId(order.getOrderNo());

        // 支付时间
        if (order.getPayTime() != null) {
            payInfo.setPayDate(order.getPayTime().format(dateTimeFormatter));
        } else {
            // 如果没有支付时间，使用订单创建时间
            payInfo.setPayDate(order.getCreateTime().format(dateTimeFormatter));
        }

        // 支付方式（转换为中文传值）
        String paymentMethod = order.getPaymentMethod();
        if (paymentMethod != null) {
            // 映射支付方式为中文：ALIPAY -> 支付宝, WECHAT -> 微信, PRE_DEPOSIT -> 预存款支付
            if ("ALIPAY".equalsIgnoreCase(paymentMethod)) {
                payInfo.setPayment("支付宝");
            } else if ("WECHAT".equalsIgnoreCase(paymentMethod)) {
                payInfo.setPayment("微信");
            } else if ("PRE_DEPOSIT".equalsIgnoreCase(paymentMethod)) {
                payInfo.setPayment("预存款支付");
            } else {
                payInfo.setPayment("其他");
            }
        } else {
            payInfo.setPayment("其他");
        }

        // 实付金额（必填，必须等于 pay_amount）
        payInfo.setAmount(order.getActualAmount());

        // 卖家账号（转换为中文传值）
        if (paymentMethod != null) {
            if ("ALIPAY".equalsIgnoreCase(paymentMethod)) {
                payInfo.setSellerAccount("支付宝");
            } else if ("WECHAT".equalsIgnoreCase(paymentMethod)) {
                payInfo.setSellerAccount("微信");
            } else if ("PRE_DEPOSIT".equalsIgnoreCase(paymentMethod)) {
                payInfo.setSellerAccount("预存款支付");
            } else {
                payInfo.setSellerAccount("其他");
            }
        } else {
            payInfo.setSellerAccount("其他");
        }

        // 买家账号：传买家的手机号
        String buyerAccount = getBuyerPhone(order.getUserId());
        // 如果获取不到用户手机号，使用收货人手机号
        if (buyerAccount == null || buyerAccount.isEmpty()) {
            buyerAccount = dto.getReceiverMobile();
        }
        // 如果还是没有，使用用户ID或订单号
        if (buyerAccount == null || buyerAccount.isEmpty()) {
            buyerAccount = order.getUserId() != null ? String.valueOf(order.getUserId()) : order.getOrderNo();
        }
        payInfo.setBuyerAccount(buyerAccount);

        dto.setPay(payInfo);

        return dto;
    }

    /**
     * 获取买家姓名
     */
    private String getBuyerName(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            User user = userRepository.selectById(userId);
            if (user != null && user.getRealName() != null && !user.getRealName().isEmpty()) {
                return user.getRealName();
            }
        } catch (Exception e) {
            log.warn("获取用户姓名失败: userId={}", userId, e);
        }
        return null;
    }

    /**
     * 获取买家手机号
     */
    private String getBuyerPhone(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            User user = userRepository.selectById(userId);
            if (user != null && user.getPhone() != null && !user.getPhone().isEmpty()) {
                return user.getPhone();
            }
        } catch (Exception e) {
            log.warn("获取用户手机号失败: userId={}", userId, e);
        }
        return null;
    }
}
