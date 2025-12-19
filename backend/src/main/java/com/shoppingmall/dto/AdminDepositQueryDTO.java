package com.shoppingmall.dto;

import lombok.Data;

/**
 * 管理后台预存款查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class AdminDepositQueryDTO {

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名（模糊搜索）
     */
    private String username;

    /**
     * 事件类型（在线充值、预存款支付、预存款退款、代充值）
     */
    private String event;

    /**
     * 类型（1-充值，2-消费，3-退款）
     */
    private Integer type;

    /**
     * 状态（0-待审核，1-已通过，2-已拒绝，3-支付中，4-已超时）
     */
    private Integer status;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 外部交易号
     */
    private String externalTradeNo;

    /**
     * 内部订单号
     */
    private String internalOrderNo;

    /**
     * 起始时间（格式：YYYY-MM-DD）
     */
    private String startDate;

    /**
     * 结束时间（格式：YYYY-MM-DD）
     */
    private String endDate;
}
































