package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 订单问题/消息VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class OrderMessageVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 用户真实姓名
     */
    private String realName;

    /**
     * 消息类型：1-我已付款，2-我有问题
     */
    private Integer messageType;

    /**
     * 消息类型文本
     */
    private String messageTypeText;

    /**
     * 问题标题（我有问题时必填）
     */
    private String title;

    /**
     * 问题内容（我有问题时必填）
     */
    private String content;

    /**
     * 付款金额（我已付款时必填）
     */
    private BigDecimal paymentAmount;

    /**
     * 付款方式（我已付款时必填）
     */
    private String paymentMethod;

    /**
     * 付款日期（我已付款时必填）
     */
    private LocalDate paymentDate;

    /**
     * 付款时间（我已付款时必填）
     */
    private LocalTime paymentTime;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 处理状态：0-待处理，1-处理中，2-已处理，3-已关闭
     */
    private Integer status;

    /**
     * 处理状态文本
     */
    private String statusText;

    /**
     * 处理人ID（管理员）
     */
    private Long handlerId;

    /**
     * 处理人姓名
     */
    private String handlerName;

    /**
     * 处理时间
     */
    private LocalDateTime handleTime;

    /**
     * 处理备注
     */
    private String handleRemark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}










