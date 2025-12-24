package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.JushuitanLogisticsDTO;
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
import java.util.List;

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

    private final ObjectMapper objectMapper = new ObjectMapper();
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

        // 记录同步日志
        OrderSyncLog syncLog = new OrderSyncLog();
        syncLog.setOrderId(orderId);
        syncLog.setOrderNo(order.getOrderNo());
        syncLog.setSyncType("PULL_LOGISTICS");
        syncLog.setSyncStatus(2); // 处理中
        syncLog.setRetryCount(0);

        try {
            // 从聚水潭查询物流信息
            List<JushuitanLogisticsDTO> logisticsList = jushuitanApiService.queryLogistics(order.getOrderNo());

            // 记录响应数据
            syncLog.setResponseData(objectMapper.writeValueAsString(logisticsList));

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
        // 查询所有已同步到ERP但未发货的订单
        List<Order> pendingOrders = orderRepository.selectList(
            new QueryWrapper<Order>()
                .eq("erp_sync_status", 1) // 已同步到ERP
                .eq("order_status", 1)     // 已付款未发货
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
}
