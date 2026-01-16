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











































