package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class PaymentRecordVO {
    /**
     * 主键ID
     */
    private Long id;

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
     * 用户名
     */
    private String username;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 已退款金额
     */
    private BigDecimal refundedAmount;

    /**
     * 可退款金额
     */
    private BigDecimal refundableAmount;

    /**
     * 支付状态
     */
    private Integer paymentStatus;

    /**
     * 支付状态名称
     */
    private String paymentStatusName;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 退款时间
     */
    private LocalDateTime refundTime;

    /**
     * 退款原因
     */
    private String refundReason;

    /**
     * 退款操作人姓名
     */
    private String refundOperatorName;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}



















