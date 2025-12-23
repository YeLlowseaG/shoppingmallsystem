package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 订单退款审核DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-19
 */
@Data
public class OrderRefundAuditDTO {

    /**
     * 退款申请ID
     */
    @NotNull(message = "退款申请ID不能为空")
    private Long refundId;

    /**
     * 审核结果（1-通过，2-拒绝）
     */
    @NotNull(message = "审核结果不能为空")
    private Integer auditResult;

    /**
     * 审核备注
     */
    private String auditRemark;
}

