package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理后台预存款交易记录VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class AdminDepositRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 事件描述
     */
    private String event;

    /**
     * 类型（1-充值，2-消费，3-退款）
     */
    private Integer type;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 状态（0-待审核，1-已通过，2-已拒绝，3-支付中，4-已超时）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

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
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 关联订单ID
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
     * 内部订单号（系统生成，用于跟踪）
     */
    private String internalOrderNo;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;
}










































