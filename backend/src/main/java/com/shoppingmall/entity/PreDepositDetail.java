package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预存款明细实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
@TableName("pre_deposit_detail")
public class PreDepositDetail {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 金额（原始金额，用于兼容旧数据）
     */
    private BigDecimal amount;

    /**
     * 存入金额
     */
    private BigDecimal depositAmount;

    /**
     * 支出金额
     */
    private BigDecimal expenseAmount;

    /**
     * 冻结金额
     */
    private BigDecimal frozenAmount;

    /**
     * 解冻金额
     */
    private BigDecimal unfrozenAmount;

    /**
     * 当前余额
     */
    private BigDecimal currentBalance;

    /**
     * 可用余额
     */
    private BigDecimal availableBalance;

    /**
     * 事件描述（如：预存款支付、在线充值、预存款退款、代充值）
     */
    private String event;

    /**
     * 类型（1-充值，2-消费，3-退款）
     */
    private Integer type;

    /**
     * 状态（0-待审核，1-已通过，2-已拒绝/支付失败，3-支付中，4-已超时）
     * 注意：状态2根据支付方式显示不同文案
     * - 线上充值（支付宝/微信）：显示为"支付失败"
     * - 线下充值/代充值：显示为"已拒绝"
     */
    private Integer status;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付凭证URL
     */
    private String paymentVoucher;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联订单ID（如果是订单相关操作）
     */
    private Long orderId;

    /**
     * 关联订单号
     */
    private String orderNo;

    /**
     * 外部交易号（微信/支付宝返回的交易号）
     */
    private String externalTradeNo;

    /**
     * 内部订单号（用于充值记录查找）
     */
    private String internalOrderNo;

    /**
     * 回调数据（JSON格式，保存第三方支付返回的原始数据）
     */
    private String callbackData;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

