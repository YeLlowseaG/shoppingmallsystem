package com.shoppingmall.service.buyer.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.constant.OrderStatus;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.service.buyer.OrderScheduledService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final ProductStockRepository productStockRepository;

    /**
     * 订单支付超时时间（小时），从配置文件读取，默认6小时
     */
    @Value("${order.payment-timeout-hours:6}")
    private Integer paymentTimeoutHours;

    /**
     * 自动取消超时的待付款订单
     * 每分钟执行一次，检查超过指定时间未支付的订单
     */
    @Override
    @Scheduled(fixedRate = 60000) // 每分钟执行一次（60000毫秒）
    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeoutOrders() {
        try {
            LocalDateTime timeoutThreshold = LocalDateTime.now().minusHours(paymentTimeoutHours);

            // 查找超时的待付款订单
            List<Order> timeoutOrders = orderRepository.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(Order::getOrderStatus, OrderStatus.PENDING_PAYMENT)
                            .eq(Order::getPaymentStatus, 0) // 未支付
                            .le(Order::getCreateTime, timeoutThreshold)
            );

            if (timeoutOrders.isEmpty()) {
                return;
            }

            log.info("发现{}个超时的待付款订单，开始自动取消", timeoutOrders.size());

            // 批量处理超时订单
            for (Order order : timeoutOrders) {
                try {
                    // 恢复库存
                    LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
                    itemWrapper.eq(OrderItem::getOrderId, order.getId());
                    List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

                    for (OrderItem orderItem : orderItems) {
                        // 恢复product表的库存
                        Product product = productRepository.selectById(orderItem.getProductId());
                        if (product != null && product.getStock() != null) {
                            product.setStock(product.getStock() + orderItem.getQuantity());
                            productRepository.updateById(product);
                        }

                        // 恢复product_stock表的库存
                        LambdaQueryWrapper<ProductStock> stockWrapper = new LambdaQueryWrapper<>();
                        stockWrapper.eq(ProductStock::getProductId, orderItem.getProductId());
                        ProductStock productStock = productStockRepository.selectOne(stockWrapper);

                        if (productStock != null) {
                            // 减少锁定库存
                            int newLockedStock = (productStock.getLockedStock() != null ? productStock.getLockedStock() : 0) - orderItem.getQuantity();
                            if (newLockedStock < 0) {
                                newLockedStock = 0;
                            }
                            productStock.setLockedStock(newLockedStock);

                            // 增加可用库存
                            int newAvailableStock = (productStock.getAvailableStock() != null ? productStock.getAvailableStock() : 0) + orderItem.getQuantity();
                            productStock.setAvailableStock(newAvailableStock);
                            productStockRepository.updateById(productStock);
                        }
                    }

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
        } catch (Exception e) {
            log.error("自动取消超时订单任务执行异常", e);
            // 定时任务异常不影响系统运行
        }
    }
}
































