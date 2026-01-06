package com.shoppingmall.dto;

import lombok.Data;

/**
 * 支付接口日志查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class PaymentApiLogQueryDTO {
    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页数量
     */
    private Integer pageSize = 20;

    /**
     * 支付方式（ALIPAY-支付宝，WECHAT-微信）
     */
    private String paymentMethod;

    /**
     * 接口类型（CREATE_PAYMENT-创建支付，REFUND-退款，QUERY_ORDER-查询订单，CALLBACK-回调通知）
     */
    private String apiType;

    /**
     * 业务类型（ORDER-订单支付，DEPOSIT-预存款充值）
     */
    private String businessType;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 接口调用状态（0-失败，1-成功，2-处理中）
     */
    private Integer apiStatus;

    /**
     * 开始时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String startTime;

    /**
     * 结束时间（格式：yyyy-MM-dd HH:mm:ss）
     */
    private String endTime;
}













