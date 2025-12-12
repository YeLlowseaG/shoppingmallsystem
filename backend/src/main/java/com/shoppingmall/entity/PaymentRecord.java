package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
@TableName("payment_record")
public class PaymentRecord {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 支付流水号（唯一）
     */
    private String paymentNo;

    /**
     * 支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）
     */
    private String paymentMethod;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态（0-待支付，1-已支付，2-已退款，3-已失败）
     */
    private Integer paymentStatus;

    /**
     * 支付时间
     */
    private LocalDateTime paymentTime;

    /**
     * 回调数据（JSON格式）
     */
    private String callbackData;

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

