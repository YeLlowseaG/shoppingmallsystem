package com.shoppingmall.dto;

import lombok.Data;

/**
 * 支付记录查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-13
 */
@Data
public class PaymentRecordQueryDTO {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 10;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 支付方式（WECHAT/ALIPAY/PRE_DEPOSIT）
     */
    private String paymentMethod;

    /**
     * 支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）
     */
    private Integer paymentStatus;

    /**
     * 开始时间（格式：yyyy-MM-dd）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd）
     */
    private String endTime;
}


