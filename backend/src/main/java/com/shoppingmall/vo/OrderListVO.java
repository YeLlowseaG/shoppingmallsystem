package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单列表VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class OrderListVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 收货人姓名
     */
    private String recipientName;

    /**
     * 收货人地址
     */
    private String recipientAddress;

    /**
     * 订单描述（商品描述）
     */
    private String description;

    /**
     * 下单日期
     */
    private LocalDateTime orderDate;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）
     */
    private Integer status;

    /**
     * 订单状态文本
     */
    private String statusText;

    /**
     * 物流信息
     */
    private LogisticsInfo logistics;

    /**
     * 物流信息内部类
     */
    @Data
    public static class LogisticsInfo {
        /**
         * 发货日期
         */
        private String shipDate;

        /**
         * 发货时间
         */
        private String shipTime;

        /**
         * 承运公司
         */
        private String carrier;

        /**
         * 发货单号
         */
        private String trackingNo;
    }
}





