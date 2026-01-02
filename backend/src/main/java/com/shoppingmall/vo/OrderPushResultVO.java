package com.shoppingmall.vo;

import lombok.Data;

/**
 * 订单推送结果VO（包含详细日志）
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Data
public class OrderPushResultVO {

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * ERP订单ID
     */
    private String erpOrderId;

    /**
     * 推送日志（详细信息）
     */
    private String pushLog;

    /**
     * 请求数据
     */
    private String requestData;

    /**
     * 响应数据
     */
    private String responseData;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误码
     */
    private String errorCode;
}
