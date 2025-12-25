package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 订单问题/消息DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class OrderMessageDTO {

    /**
     * 订单号
     */
    @NotBlank(message = "订单号不能为空")
    private String orderNumber;

    /**
     * 消息类型：paid-我已付款，question-我有问题
     */
    @NotBlank(message = "消息类型不能为空")
    private String messageType;

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
     * 付款小时（0-23）
     */
    private Integer paymentHour;

    /**
     * 付款分钟（0-59）
     */
    private Integer paymentMinute;

    /**
     * 备注
     */
    private String remarks;
}

















