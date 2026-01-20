package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.service.buyer.OrderScheduledService;
import com.shoppingmall.service.system.SystemConfigService;
import com.shoppingmall.util.ScheduledTaskLogUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderScheduledServiceImpl implements OrderScheduledService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final SystemConfigService systemConfigService;
    private final ScheduledTaskLogUtil scheduledTaskLogUtil;

    /**
     * 获取订单支付超时时间（小时），从数据库配置读取，默认4小时
     * 每次执行定时任务时读取最新配置，确保配置修改后立即生效
     *
     * @return 支付超时时间（小时）
     */
    private Integer getPaymentTimeoutHours() {
        String timeoutStr = systemConfigService.getConfigValue("order.payment-timeout-hours", "4");
        try {
            return Integer.parseInt(timeoutStr);
        } catch (NumberFormatException e) {
            log.warn("订单支付超时时间配置格式错误，使用默认值4小时: {}", timeoutStr);
            return 4;
        }
    }

    /**
     * 获取自动确认收货天数，从数据库配置读取，默认15天
     * 每次执行定时任务时读取最新配置，确保配置修改后立即生效
     *
     * @return 自动确认收货天数
     */
    private Integer getAutoConfirmReceiptDays() {
        String daysStr = systemConfigService.getConfigValue("order.auto-confirm-receipt-days", "15");
        try {
            return Integer.parseInt(daysStr);
        } catch (NumberFormatException e) {
            log.warn("自动确认收货天数配置格式错误，使用默认值15天: {}", daysStr);
            return 15;
        }
    }

    /**
     * 自动取消超时的待付款订单
     * 每分钟执行一次，检查超过指定时间未支付的订单
     */
    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次（60000毫秒）
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        boolean success = false;

        try {
            // 每次执行时读取最新配置
            Integer paymentTimeoutHours = getPaymentTimeoutHours();
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(paymentTimeoutHours);

            // 查找超时的待付款订单
            List<Order> timeoutOrders = orderRepository.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getOrderStatus, OrderStatus.PENDING_PAYMENT)
                            .eq(Order::getPaymentStatus, 0) // 未支付
                            .le(Order::getCreateTime, timeoutThreshold)
            );

            if (timeoutOrders.isEmpty()) {
                success = true;
                return;
            }

            log.info("发现{}个超时的待付款订单，开始自动取消", timeoutOrders.size());

            // 批量处理超时订单
            for (Order order : timeoutOrders) {
                try {
                    // 超时订单都是待付款状态，创建订单时已经扣减了库存，取消时需要恢复库存
                    LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                    itemWrapper.eq(OrderItem::getOrderId, order.getId());
                    List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

                    for (OrderItem orderItem : orderItems) {
                        if (orderItem.getSkuId() != null) {
                            // 有SKU，恢复SKU库存
                            ProductSku sku = productSkuRepository.selectById(orderItem.getSkuId());
                            if (sku != null && sku.getStock() != null) {
                                sku.setStock(sku.getStock() + orderItem.getQuantity());
                                productSkuRepository.updateById(sku);
                            }
                            
                            // 同步更新商品总库存（从所有SKU汇总）
                            updateProductTotalStockFromSkus(orderItem.getProductId());
                        } else {
                            // 无SKU，恢复商品库存
                            Product product = productRepository.selectById(orderItem.getProductId());
                            if (product != null && product.getStock() != null) {
                                product.setStock(product.getStock() + orderItem.getQuantity());
                                productRepository.updateById(product);
                            }
                        }
                    }
                    
                    log.info("超时订单取消，已恢复库存: orderNo={}", order.getOrderNo());

                    // 更新订单状态为已取消
                    order.setOrderStatus(OrderStatus.CANCELLED);
                    orderRepository.updateById(order);

                    log.info("自动取消超时订单成功: orderNo={}, createTime={}", order.getOrderNo(), order.getCreateTime());
                } catch (Exception e) {
                    log.error("自动取消订单失败: orderNo={}", order.getOrderNo(), e);
                    // 继续处理下一个订单，不中断整个任务
                }
            }

            log.info("自动取消超时订单任务完成，共处理{}个订单", timeoutOrders.size());
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getName();
            }
            log.error("自动取消超时订单任务执行异常", e);
            // 定时任务异常不影响系统运行
        } finally {
            // 记录执行日志
            scheduledTaskLogUtil.logAutoExecution(
                "订单自动取消超时订单",
                "订单管理",
                "orderScheduledServiceImpl",
                "cancelTimeoutOrders",
                startTime,
                success,
                errorMessage
            );
        }
    }

    /**
     * 自动确认收货
     * 每天凌晨12点30分执行一次，对已发货超过指定天数的订单自动确认收货
     */
    @Override
    @Scheduled(cron = "0 30 0 * * ?") // 每天凌晨12点30分执行
    @Transactional(rollbackFor = Exception.class)
    public void autoConfirmReceipt() {
        LocalDateTime startTime = LocalDateTime.now();
        String errorMessage = null;
        boolean success = false;
        int processedCount = 0;

        try {
            // 每次执行时读取最新配置
            Integer autoConfirmDays = getAutoConfirmReceiptDays();
            LocalDateTime thresholdTime = LocalDateTime.now().minusDays(autoConfirmDays);

            // 查找已发货且发货时间超过指定天数的订单
            List<Order> shippedOrders = orderRepository.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getOrderStatus, OrderStatus.SHIPPED) // 已发货状态
                            .isNotNull(Order::getShipTime) // 发货时间不为空
                            .le(Order::getShipTime, thresholdTime) // 发货时间早于阈值时间
            );

            if (shippedOrders.isEmpty()) {
                log.info("没有需要自动确认收货的订单");
                success = true;
                return;
            }

            log.info("发现{}个已发货超过{}天的订单，开始自动确认收货", shippedOrders.size(), autoConfirmDays);

            // 批量处理订单
            for (Order order : shippedOrders) {
                try {
                    // 更新订单状态为已完成
                    order.setOrderStatus(OrderStatus.COMPLETED);
                    order.setCompleteTime(LocalDateTime.now());
                    
                    // 添加备注（如果原备注不为空，追加新备注；否则直接设置）
                    String remark = "系统自动确认收货";
                    if (order.getOrderRemark() != null && !order.getOrderRemark().trim().isEmpty()) {
                        order.setOrderRemark(order.getOrderRemark() + "\n" + remark);
                    } else {
                        order.setOrderRemark(remark);
                    }
                    
                    orderRepository.updateById(order);

                    // 增加商品销量（订单完成时）
                    updateProductSalesCount(order.getId(), true);

                    processedCount++;
                    log.info("自动确认收货成功: orderNo={}, shipTime={}, autoConfirmDays={}", 
                            order.getOrderNo(), order.getShipTime(), autoConfirmDays);
                } catch (Exception e) {
                    log.error("自动确认收货失败: orderNo={}", order.getOrderNo(), e);
                    // 继续处理下一个订单，不中断整个任务
                }
            }

            log.info("自动确认收货任务完成，共处理{}个订单", processedCount);
            success = true;
        } catch (Exception e) {
            errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
                errorMessage = e.getClass().getName();
            }
            log.error("自动确认收货任务执行异常", e);
            // 定时任务异常不影响系统运行
        } finally {
            // 记录执行日志
            scheduledTaskLogUtil.logAutoExecution(
                "订单自动确认收货",
                "订单管理",
                "orderScheduledServiceImpl",
                "autoConfirmReceipt",
                startTime,
                success,
                errorMessage
            );
        }
    }

    /**
     * 更新商品销量
     * 
     * @param orderId 订单ID
     * @param increase 是否增加销量（true-增加，false-扣减）
     */
    private void updateProductSalesCount(Long orderId, boolean increase) {
        try {
            // 查询订单商品
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);
            
            for (OrderItem orderItem : orderItems) {
                Product product = productRepository.selectById(orderItem.getProductId());
                if (product != null) {
                    int currentSalesCount = product.getSalesCount() != null ? product.getSalesCount() : 0;
                    int quantity = orderItem.getQuantity() != null ? orderItem.getQuantity() : 0;
                    
                    if (increase) {
                        // 增加销量
                        product.setSalesCount(currentSalesCount + quantity);
                    } else {
                        // 扣减销量（确保不为负数）
                        int newSalesCount = currentSalesCount - quantity;
                        product.setSalesCount(Math.max(0, newSalesCount));
                    }
                    
                    productRepository.updateById(product);
                    log.debug("更新商品销量: productId={}, increase={}, quantity={}, newSalesCount={}", 
                            product.getId(), increase, quantity, product.getSalesCount());
                }
            }
        } catch (Exception e) {
            log.error("更新商品销量失败: orderId={}", orderId, e);
            // 不影响主流程
        }
    }

    /**
     * 从SKU汇总更新商品总库存
     */
    private void updateProductTotalStockFromSkus(Long productId) {
        try {
            LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
            skuWrapper.eq(ProductSku::getProductId, productId);
            List<ProductSku> skus = productSkuRepository.selectList(skuWrapper);

            if (!skus.isEmpty()) {
                int totalStock = skus.stream()
                        .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                        .sum();

                Product product = productRepository.selectById(productId);
                if (product != null) {
                    product.setStock(totalStock);
                    productRepository.updateById(product);
                    log.debug("从SKU汇总更新商品总库存: productId={}, 总库存={}", productId, totalStock);
                }
            }
        } catch (Exception e) {
            log.error("从SKU汇总更新商品总库存失败: productId={}", productId, e);
            // 不影响主流程
        }
    }
}











































