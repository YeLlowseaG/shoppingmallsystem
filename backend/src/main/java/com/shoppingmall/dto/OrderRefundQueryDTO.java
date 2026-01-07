package com.shoppingmall.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订单退款查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-20
 */
@Data
public class OrderRefundQueryDTO {

    /**
     * 退款单号
     */
    private String refundNo;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 退款状态（3-退款中，4-退款成功，5-退款失败）
     */
    private Integer refundStatus;

    /**
     * 退款类型（1-部分退款，2-全额退款）
     */
    private Integer refundType;

    /**
     * 操作人ID（管理员）
     */
    private Long operatorId;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}



































