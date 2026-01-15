package com.shoppingmall.common.util;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.OrderItem;
import com.shoppingmall.repository.order.OrderItemRepository;
import com.shoppingmall.repository.order.OrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存工具类
 * 用于计算可用库存（总库存 - 未支付订单锁定库存）
 *
 * @author ShoppingMall Team
 * @date 2026-01-15
 */
@Slf4j
@Component
public class StockUtil {

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private OrderItemRepository orderItemRepository;

    /**
     * 订单状态：待付款（0）
     */
    private static final Integer ORDER_STATUS_PENDING_PAYMENT = 0;

    /**
     * 计算商品的可用库存
     * 可用库存 = 总库存 - 未支付订单的锁定数量
     *
     * @param productId 商品ID
     * @param totalStock 总库存
     * @return 可用库存
     */
    public int calculateAvailableStock(Long productId, Integer totalStock) {
        if (productId == null || totalStock == null) {
            return 0;
        }

        // 查询该商品在未支付订单中的锁定数量
        int lockedQuantity = getLockedQuantityByProductId(productId);

        // 可用库存 = 总库存 - 锁定数量
        int availableStock = totalStock - lockedQuantity;
        return Math.max(0, availableStock); // 确保不为负数
    }

    /**
     * 计算SKU的可用库存
     * 可用库存 = SKU总库存 - 未支付订单中该SKU的锁定数量
     *
     * @param skuId SKU ID
     * @param totalStock SKU总库存
     * @return 可用库存
     */
    public int calculateSkuAvailableStock(Long skuId, Integer totalStock) {
        if (skuId == null || totalStock == null) {
            return 0;
        }

        // 查询该SKU在未支付订单中的锁定数量
        int lockedQuantity = getLockedQuantityBySkuId(skuId);

        // 可用库存 = 总库存 - 锁定数量
        int availableStock = totalStock - lockedQuantity;
        return Math.max(0, availableStock); // 确保不为负数
    }

    /**
     * 批量计算商品的可用库存
     *
     * @param productStockMap 商品ID -> 总库存的映射
     * @return 商品ID -> 可用库存的映射
     */
    public Map<Long, Integer> batchCalculateAvailableStock(Map<Long, Integer> productStockMap) {
        if (productStockMap == null || productStockMap.isEmpty()) {
            return Map.of();
        }

        // 批量查询所有商品的锁定数量
        Map<Long, Integer> lockedQuantityMap = batchGetLockedQuantityByProductIds(productStockMap.keySet());

        // 计算每个商品的可用库存
        return productStockMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int lockedQuantity = lockedQuantityMap.getOrDefault(entry.getKey(), 0);
                            int availableStock = entry.getValue() - lockedQuantity;
                            return Math.max(0, availableStock);
                        }
                ));
    }

    /**
     * 批量计算SKU的可用库存
     *
     * @param skuStockMap SKU ID -> 总库存的映射
     * @return SKU ID -> 可用库存的映射
     */
    public Map<Long, Integer> batchCalculateSkuAvailableStock(Map<Long, Integer> skuStockMap) {
        if (skuStockMap == null || skuStockMap.isEmpty()) {
            return Map.of();
        }

        // 批量查询所有SKU的锁定数量
        Map<Long, Integer> lockedQuantityMap = batchGetLockedQuantityBySkuIds(skuStockMap.keySet());

        // 计算每个SKU的可用库存
        return skuStockMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            int lockedQuantity = lockedQuantityMap.getOrDefault(entry.getKey(), 0);
                            int availableStock = entry.getValue() - lockedQuantity;
                            return Math.max(0, availableStock);
                        }
                ));
    }

    /**
     * 获取商品在未支付订单中的锁定数量
     *
     * @param productId 商品ID
     * @return 锁定数量
     */
    private int getLockedQuantityByProductId(Long productId) {
        try {
            // 查询所有待付款订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderStatus, ORDER_STATUS_PENDING_PAYMENT);
            List<Order> pendingOrders = orderRepository.selectList(orderWrapper);

            if (pendingOrders.isEmpty()) {
                return 0;
            }

            // 获取订单ID列表
            List<Long> orderIds = pendingOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            // 查询这些订单中该商品的数量
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            itemWrapper.eq(OrderItem::getProductId, productId);
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 汇总数量
            return orderItems.stream()
                    .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                    .sum();
        } catch (Exception e) {
            log.error("查询商品锁定数量失败: productId={}", productId, e);
            return 0; // 出错时返回0，避免影响主流程
        }
    }

    /**
     * 获取SKU在未支付订单中的锁定数量
     *
     * @param skuId SKU ID
     * @return 锁定数量
     */
    private int getLockedQuantityBySkuId(Long skuId) {
        try {
            // 查询所有待付款订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderStatus, ORDER_STATUS_PENDING_PAYMENT);
            List<Order> pendingOrders = orderRepository.selectList(orderWrapper);

            if (pendingOrders.isEmpty()) {
                return 0;
            }

            // 获取订单ID列表
            List<Long> orderIds = pendingOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            // 查询这些订单中该SKU的数量
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            itemWrapper.eq(OrderItem::getSkuId, skuId);
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 汇总数量
            return orderItems.stream()
                    .mapToInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                    .sum();
        } catch (Exception e) {
            log.error("查询SKU锁定数量失败: skuId={}", skuId, e);
            return 0; // 出错时返回0，避免影响主流程
        }
    }

    /**
     * 批量获取商品在未支付订单中的锁定数量
     *
     * @param productIds 商品ID集合
     * @return 商品ID -> 锁定数量的映射
     */
    private Map<Long, Integer> batchGetLockedQuantityByProductIds(java.util.Set<Long> productIds) {
        try {
            // 查询所有待付款订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderStatus, ORDER_STATUS_PENDING_PAYMENT);
            List<Order> pendingOrders = orderRepository.selectList(orderWrapper);

            if (pendingOrders.isEmpty()) {
                return Map.of();
            }

            // 获取订单ID列表
            List<Long> orderIds = pendingOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            // 查询这些订单中指定商品的数量
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            itemWrapper.in(OrderItem::getProductId, productIds);
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 按商品ID汇总数量
            return orderItems.stream()
                    .collect(Collectors.groupingBy(
                            OrderItem::getProductId,
                            Collectors.summingInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                    ));
        } catch (Exception e) {
            log.error("批量查询商品锁定数量失败", e);
            return Map.of(); // 出错时返回空映射
        }
    }

    /**
     * 批量获取SKU在未支付订单中的锁定数量
     *
     * @param skuIds SKU ID集合
     * @return SKU ID -> 锁定数量的映射
     */
    private Map<Long, Integer> batchGetLockedQuantityBySkuIds(java.util.Set<Long> skuIds) {
        try {
            // 查询所有待付款订单
            LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
            orderWrapper.eq(Order::getOrderStatus, ORDER_STATUS_PENDING_PAYMENT);
            List<Order> pendingOrders = orderRepository.selectList(orderWrapper);

            if (pendingOrders.isEmpty()) {
                return Map.of();
            }

            // 获取订单ID列表
            List<Long> orderIds = pendingOrders.stream()
                    .map(Order::getId)
                    .collect(Collectors.toList());

            // 查询这些订单中指定SKU的数量
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.in(OrderItem::getOrderId, orderIds);
            itemWrapper.in(OrderItem::getSkuId, skuIds);
            List<OrderItem> orderItems = orderItemRepository.selectList(itemWrapper);

            // 按SKU ID汇总数量
            return orderItems.stream()
                    .collect(Collectors.groupingBy(
                            OrderItem::getSkuId,
                            Collectors.summingInt(item -> item.getQuantity() != null ? item.getQuantity() : 0)
                    ));
        } catch (Exception e) {
            log.error("批量查询SKU锁定数量失败", e);
            return Map.of(); // 出错时返回空映射
        }
    }
}


