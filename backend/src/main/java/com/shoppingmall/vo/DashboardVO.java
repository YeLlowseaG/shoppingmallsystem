package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 数据看板VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class DashboardVO {

    /**
     * 今日订单数
     */
    private Long todayOrders;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 待处理订单数（待付款+已付款未发货）
     */
    private Long pendingOrders;

    /**
     * 库存预警商品数
     */
    private Long stockWarnings;

    /**
     * 总订单数
     */
    private Long totalOrders;

    /**
     * 总销售额
     */
    private BigDecimal totalSales;

    /**
     * 总用户数
     */
    private Long totalUsers;

    /**
     * 总商品数
     */
    private Long totalProducts;

    /**
     * 最近7天销售趋势数据
     */
    private List<SalesTrendData> salesTrend;

    /**
     * 最近7天订单趋势数据
     */
    private List<OrderTrendData> orderTrend;

    /**
     * 订单状态统计
     */
    private OrderStatusStatistics orderStatusStatistics;

    /**
     * 销售趋势数据
     */
    @Data
    public static class SalesTrendData {
        /**
         * 日期（格式：MM-dd）
         */
        private String date;

        /**
         * 销售额
         */
        private BigDecimal sales;
    }

    /**
     * 订单趋势数据
     */
    @Data
    public static class OrderTrendData {
        /**
         * 日期（格式：MM-dd）
         */
        private String date;

        /**
         * 订单数
         */
        private Long count;
    }

    /**
     * 订单状态统计
     */
    @Data
    public static class OrderStatusStatistics {
        /**
         * 待付款订单数
         */
        private Long pendingPayment;

        /**
         * 已付款未发货订单数
         */
        private Long paidNotShipped;

        /**
         * 已发货订单数
         */
        private Long shipped;

        /**
         * 已完成订单数
         */
        private Long completed;

        /**
         * 已取消订单数
         */
        private Long cancelled;
    }
}










