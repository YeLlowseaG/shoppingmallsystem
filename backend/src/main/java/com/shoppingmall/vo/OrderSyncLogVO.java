package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单同步日志VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class OrderSyncLogVO {

    /**
     * ID
     */
    private Long id;

    /**
     * 订单ID
     */
    private Long orderId;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 同步类型（PUSH_ORDER-推送订单，PULL_LOGISTICS-拉取物流）
     */
    private String syncType;

    /**
     * 同步类型描述
     */
    private String syncTypeDesc;

    /**
     * 同步状态（0-失败，1-成功，2-处理中）
     */
    private Integer syncStatus;

    /**
     * 同步状态描述
     */
    private String syncStatusDesc;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 请求数据（JSON格式）
     */
    private String requestData;

    /**
     * 响应数据（JSON格式）
     */
    private String responseData;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
