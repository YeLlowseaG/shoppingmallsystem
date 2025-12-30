package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.Order;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.order.OrderRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.service.admin.DashboardService;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.vo.DashboardVO;
import com.shoppingmall.vo.StockStatisticsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据看板服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Slf4j
@Service("adminDashboardService")
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final StockService stockService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd");

    @Override
    public DashboardVO getDashboardStatistics() {
        DashboardVO dashboard = new DashboardVO();

        // 获取今日开始和结束时间
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);

        // 1. 今日订单数
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.ge(Order::getCreateTime, todayStart)
                .le(Order::getCreateTime, todayEnd);
        Long todayOrders = orderRepository.selectCount(todayOrderWrapper);
        dashboard.setTodayOrders(todayOrders);

        // 2. 今日销售额
        List<Order> todayOrdersList = orderRepository.selectList(todayOrderWrapper);
        BigDecimal todaySales = todayOrdersList.stream()
                .filter(order -> order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setTodaySales(todaySales);

        // 3. 待处理订单数（待付款 + 已付款未发货）
        LambdaQueryWrapper<Order> pendingWrapper = new LambdaQueryWrapper<>();
        pendingWrapper.and(wrapper -> wrapper
                .eq(Order::getOrderStatus, 0) // 待付款
                .or()
                .eq(Order::getOrderStatus, 1) // 已付款未发货
        );
        Long pendingOrders = orderRepository.selectCount(pendingWrapper);
        dashboard.setPendingOrders(pendingOrders);

        // 4. 库存预警商品数
        StockStatisticsVO stockStatistics = stockService.getStockStatistics();
        dashboard.setStockWarnings(stockStatistics.getWarningProductCount());

        // 5. 总订单数
        Long totalOrders = orderRepository.selectCount(null);
        dashboard.setTotalOrders(totalOrders);

        // 6. 总销售额
        List<Order> allOrders = orderRepository.selectList(null);
        BigDecimal totalSales = allOrders.stream()
                .filter(order -> order.getTotalAmount() != null)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dashboard.setTotalSales(totalSales);

        // 7. 总用户数
        Long totalUsers = userRepository.selectCount(null);
        dashboard.setTotalUsers(totalUsers);

        // 8. 总商品数
        Long totalProducts = productRepository.selectCount(null);
        dashboard.setTotalProducts(totalProducts);

        // 9. 最近7天销售趋势
        List<DashboardVO.SalesTrendData> salesTrend = getSalesTrend();
        dashboard.setSalesTrend(salesTrend);

        // 10. 最近7天订单趋势
        List<DashboardVO.OrderTrendData> orderTrend = getOrderTrend();
        dashboard.setOrderTrend(orderTrend);

        // 11. 订单状态统计
        DashboardVO.OrderStatusStatistics orderStatusStatistics = getOrderStatusStatistics();
        dashboard.setOrderStatusStatistics(orderStatusStatistics);

        return dashboard;
    }

    /**
     * 获取最近7天销售趋势
     */
    private List<DashboardVO.SalesTrendData> getSalesTrend() {
        List<DashboardVO.SalesTrendData> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreateTime, start)
                    .le(Order::getCreateTime, end);

            List<Order> orders = orderRepository.selectList(wrapper);
            BigDecimal sales = orders.stream()
                    .filter(order -> order.getTotalAmount() != null)
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            DashboardVO.SalesTrendData data = new DashboardVO.SalesTrendData();
            data.setDate(date.format(DATE_FORMATTER));
            data.setSales(sales);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 获取最近7天订单趋势
     */
    private List<DashboardVO.OrderTrendData> getOrderTrend() {
        List<DashboardVO.OrderTrendData> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            LocalDateTime start = date.atStartOfDay();
            LocalDateTime end = date.atTime(23, 59, 59);

            LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
            wrapper.ge(Order::getCreateTime, start)
                    .le(Order::getCreateTime, end);

            Long count = orderRepository.selectCount(wrapper);

            DashboardVO.OrderTrendData data = new DashboardVO.OrderTrendData();
            data.setDate(date.format(DATE_FORMATTER));
            data.setCount(count);
            trend.add(data);
        }

        return trend;
    }

    /**
     * 获取订单状态统计
     */
    private DashboardVO.OrderStatusStatistics getOrderStatusStatistics() {
        DashboardVO.OrderStatusStatistics statistics = new DashboardVO.OrderStatusStatistics();

        // 待付款（orderStatus = 0）
        LambdaQueryWrapper<Order> pendingPaymentWrapper = new LambdaQueryWrapper<>();
        pendingPaymentWrapper.eq(Order::getOrderStatus, 0);
        statistics.setPendingPayment(orderRepository.selectCount(pendingPaymentWrapper));

        // 已付款未发货（orderStatus = 1）
        LambdaQueryWrapper<Order> paidNotShippedWrapper = new LambdaQueryWrapper<>();
        paidNotShippedWrapper.eq(Order::getOrderStatus, 1);
        statistics.setPaidNotShipped(orderRepository.selectCount(paidNotShippedWrapper));

        // 已发货（orderStatus = 2）
        LambdaQueryWrapper<Order> shippedWrapper = new LambdaQueryWrapper<>();
        shippedWrapper.eq(Order::getOrderStatus, 2);
        statistics.setShipped(orderRepository.selectCount(shippedWrapper));

        // 已完成（orderStatus = 3）
        LambdaQueryWrapper<Order> completedWrapper = new LambdaQueryWrapper<>();
        completedWrapper.eq(Order::getOrderStatus, 3);
        statistics.setCompleted(orderRepository.selectCount(completedWrapper));

        // 已取消（orderStatus = 4）
        LambdaQueryWrapper<Order> cancelledWrapper = new LambdaQueryWrapper<>();
        cancelledWrapper.eq(Order::getOrderStatus, 4);
        statistics.setCancelled(orderRepository.selectCount(cancelledWrapper));

        return statistics;
    }
}






























