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
     * 是否会员（0-普通用户，1-会员）
     */
    private Integer isMember;

    /**
     * 会员等级ID（关联 member_level 表，普通用户为 NULL）
     */
    private Long memberLevelId;

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







































