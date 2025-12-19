package com.shoppingmall.dto;

import lombok.Data;

/**
 * 订单问题查询DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class OrderMessageQueryDTO {

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 消息类型：1-我已付款，2-我有问题
     */
    private Integer messageType;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理，3-已关闭
     */
    private Integer status;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 当前页
     */
    private Long current;

    /**
     * 每页大小
     */
    private Long size;
}








