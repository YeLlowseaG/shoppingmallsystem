package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款申请实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-19
 */
@Data
@TableName("order_refund")
public class OrderRefund {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款单号（唯一）
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
     * 退款类型（1-部分退款，2-全额退款）
     */
    private Integer refundType;

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
     * 退款完成时间
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
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

































