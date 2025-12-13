package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 订单实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
@TableName("`order`")
public class Order {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单号（唯一）
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费
     */
    private BigDecimal shippingFee;

    /**
     * 税金
     */
    private BigDecimal tax;

    /**
     * 实付金额
     */
    private BigDecimal actualAmount;

    /**
     * 订单状态（0-待付款，1-已付款未发货，2-已发货，3-已完成，4-已取消，5-已退款，6-已退货）
     */
    private Integer orderStatus;

    /**
     * 支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）
     */
    private String paymentMethod;

    /**
     * 支付状态（0-待支付，1-支付中，2-已支付，3-已关闭，4-已失败，5-已退款）
     */
    private Integer paymentStatus;

    /**
     * 收货地址（JSON格式）
     */
    private String shippingAddress;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 配送日期
     */
    private LocalDate deliveryDate;

    /**
     * 配送时间段
     */
    private String deliveryTime;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 发货时间
     */
    private LocalDateTime shipTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


























