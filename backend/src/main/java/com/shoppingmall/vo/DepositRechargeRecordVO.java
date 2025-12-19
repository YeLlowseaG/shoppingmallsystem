package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预存款充值记录VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class DepositRechargeRecordVO {
    /**
     * 主键ID
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
     * 充值金额
     */
    private BigDecimal depositAmount;

    /**
     * 状态（0-待审核，1-已通过，2-已拒绝）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 支付方式
     */
    private String paymentMethod;

    /**
     * 支付方式名称
     */
    private String paymentMethodName;

    /**
     * 支付凭证URL
     */
    private String paymentVoucher;

    /**
     * 外部交易号
     */
    private String externalTradeNo;

    /**
     * 内部订单号
     */
    private String internalOrderNo;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}









