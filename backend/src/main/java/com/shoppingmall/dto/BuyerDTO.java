package com.shoppingmall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采购者数据传输对象
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@Data
public class BuyerDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户等级（0-普通，1-VIP，2-金牌）
     */
    private Integer userLevel;

    /**
     * 状态（0-待审核，1-已激活，2-已禁用）
     */
    private Integer status;

    /**
     * 审核状态（0-待审核，1-已通过，2-已拒绝）
     */
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditComment;
}
































