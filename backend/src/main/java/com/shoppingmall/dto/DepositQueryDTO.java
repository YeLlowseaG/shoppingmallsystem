package com.shoppingmall.dto;

import lombok.Data;

/**
 * 预存款查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class DepositQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 操作类型（deposit_payment-预存款支付，online_recharge-在线充值，deposit_refund-预存款退款，agent_recharge-代充值）
     */
    private String operationType;

    /**
     * 起始时间（格式：YYYY-MM-DD）
     */
    private String startDate;

    /**
     * 结束时间（格式：YYYY-MM-DD）
     */
    private String endDate;
}

































