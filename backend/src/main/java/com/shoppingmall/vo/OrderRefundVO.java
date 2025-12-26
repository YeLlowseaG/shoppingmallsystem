package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单退款申请VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-19
 */
@Data
public class OrderRefundVO {

    /**
     * 退款申请ID
     */
    private Long id;

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款金额（商品金额，不含运费）
     */
    private BigDecimal refundAmount;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款状态（3-退款中，4-退款成功，5-退款失败）
     */
    private Integer refundStatus;

    /**
     * 退款状态文本
     */
    private String refundStatusText;

    /**
     * 退款类型（1-部分退款，2-全额退款）
     */
    private Integer refundType;

    /**
     * 退款类型文本
     */
    private String refundTypeText;

    /**
     * 操作人ID（管理员）
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 操作时间
     */
    private LocalDateTime operatorTime;

    /**
     * 操作备注
     */
    private String operatorRemark;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款支付方式（原支付方式）
     */
    private String refundPaymentMethod;

    /**
     * 退款支付单号
     */
    private String refundPaymentNo;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 退款明细列表
     */
    private List<OrderRefundItemVO> refundItems;

    /**
     * 订单退款明细VO
     */
    @Data
    public static class OrderRefundItemVO {
        /**
         * 退款明细ID
         */
        private Long id;

        /**
         * 订单商品ID
         */
        private Long orderItemId;

        /**
         * 商品ID
         */
        private Long productId;

        /**
         * 商品名称
         */
        private String productName;

        /**
         * 商品编码
         */
        private String productCode;

        /**
         * SKU ID
         */
        private Long skuId;

        /**
         * SKU规格组合
         */
        private String specCombination;

        /**
         * 退款数量
         */
        private Integer refundQuantity;

        /**
         * 退款单价
         */
        private BigDecimal refundPrice;

        /**
         * 退款小计
         */
        private BigDecimal refundSubtotal;
    }
}










