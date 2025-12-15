package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预存款交易记录VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class DepositRecordVO {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 事件描述
     */
    private String event;

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
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 关联订单号
     */
    private String orderNo;
}



























