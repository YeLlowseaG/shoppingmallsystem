package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class OrderDetailVO {

    /**
     * 订单ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 原订单号
     */
    private String originalOrderNo;

    /**
     * 买家姓名（用户真实姓名）
     */
    private String buyerName;

    /**
     * 买家用户名
     */
    private String buyerUsername;

    /**
     * 下单日期
     */
    private LocalDateTime orderDate;

    /**
     * 订单状态（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）
     */
    private Integer status;

    /**
     * 订单状态文本
     */
    private String statusText;

    /**
     * 订单商品列表
     */
    private List<OrderItemVO> items;

    /**
     * 收货人信息
     */
    private RecipientInfo recipientInfo;

    /**
     * 订单附言
     */
    private String orderNotes;

    /**
     * 商品总数量
     */
    private Integer totalQuantity;

    /**
     * 商品总金额
     */
    private BigDecimal totalProductAmount;

    /**
     * 配送费用
     */
    private BigDecimal shippingFee;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单历史记录
     */
    private List<OrderHistoryVO> orderHistory;

    /**
     * 订单商品VO
     */
    @Data
    public static class OrderItemVO {
        /**
         * 商品ID
         */
        private Long id;

        /**
         * 商品编码
         */
        private String productCode;

        /**
         * 商品名称
         */
        private String name;

        /**
         * SKU规格组合
         */
        private String specCombination;

        /**
         * 商品图片
         */
        private String image;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 数量
         */
        private Integer quantity;

        /**
         * 已退款数量
         */
        private Integer refundedQuantity;

        /**
         * 可退款数量（订单数量 - 已退款数量）
         */
        private Integer availableRefundQuantity;

        /**
         * 小计
         */
        private BigDecimal subtotal;
    }

    /**
     * 收货人信息VO
     */
    @Data
    public static class RecipientInfo {
        /**
         * 收货人姓名
         */
        private String name;

        /**
         * 配送地区
         */
        private String region;

        /**
         * 收货人邮编
         */
        private String zipCode;

        /**
         * 配送方式
         */
        private String shippingMethod;

        /**
         * 商品重量
         */
        private BigDecimal weight;

        /**
         * 收货人地址
         */
        private String address;

        /**
         * 收货人Mail
         */
        private String email;

        /**
         * 联系电话
         */
        private String phone;

        /**
         * 送货时间
         */
        private String deliveryTime;

        /**
         * 付款方式
         */
        private String paymentMethod;

        /**
         * 支付币别
         */
        private String paymentCurrency;
    }

    /**
     * 订单历史记录VO
     */
    @Data
    public static class OrderHistoryVO {
        /**
         * 日期时间
         */
        private LocalDateTime date;

        /**
         * 操作描述
         */
        private String action;
    }
}

















































