package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 支付接口日志VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
@Data
public class PaymentApiLogVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 支付方式（ALIPAY-支付宝，WECHAT-微信）
     */
    private String paymentMethod;

    /**
     * 接口类型（CREATE_PAYMENT-创建支付，REFUND-退款，QUERY_ORDER-查询订单，CALLBACK-回调通知）
     */
    private String apiType;

    /**
     * 接口类型描述
     */
    private String apiTypeDesc;

    /**
     * 业务类型（ORDER-订单支付，DEPOSIT-预存款充值）
     */
    private String businessType;

    /**
     * 业务类型描述
     */
    private String businessTypeDesc;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 支付流水号
     */
    private String paymentNo;

    /**
     * 外部交易号
     */
    private String externalTradeNo;

    /**
     * 接口URL
     */
    private String apiUrl;

    /**
     * HTTP请求方法（GET/POST）
     */
    private String requestMethod;

    /**
     * 请求数据（JSON/XML格式）
     */
    private String requestData;

    /**
     * 响应数据（JSON/XML格式）
     */
    private String responseData;

    /**
     * HTTP状态码
     */
    private Integer httpStatusCode;

    /**
     * 接口调用状态（0-失败，1-成功，2-处理中）
     */
    private Integer apiStatus;

    /**
     * 接口状态描述
     */
    private String apiStatusDesc;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行耗时（毫秒）
     */
    private Integer executionTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}















