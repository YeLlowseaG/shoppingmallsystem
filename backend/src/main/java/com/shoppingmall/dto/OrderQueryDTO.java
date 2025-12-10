package com.shoppingmall.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 订单查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class OrderQueryDTO {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 收货人姓名
     */
    private String recipientName;

    /**
     * 订单状态（前端传入字符串：pending_payment, paid_not_shipped, shipped, completed, cancelled, refunded, returned）
     * 后端会自动转换为数字状态
     */
    private String orderStatus;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系手机
     */
    private String contactMobile;

    /**
     * 收货人地址
     */
    private String recipientAddress;

    /**
     * 页码（从1开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页条数
     */
    private Integer pageSize = 10;
}

