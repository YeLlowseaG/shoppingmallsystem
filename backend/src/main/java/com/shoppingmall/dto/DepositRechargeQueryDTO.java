package com.shoppingmall.dto;

import lombok.Data;

/**
 * 预存款充值记录查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class DepositRechargeQueryDTO {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 状态（0-待审核，1-已通过，2-已拒绝）
     */
    private Integer status;

    /**
     * 开始时间（格式：yyyy-MM-dd）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd）
     */
    private String endTime;
}


