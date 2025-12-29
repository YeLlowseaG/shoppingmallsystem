package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单退款明细实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-19
 */
@Data
@TableName("order_refund_item")
public class OrderRefundItem {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 退款申请ID
     */
    private Long refundId;

    /**
     * 订单商品ID
     */
    private Long orderItemId;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品名称（快照）
     */
    private String productName;

    /**
     * 商品编码（快照）
     */
    private String productCode;

    /**
     * SKU ID（快照）
     */
    private Long skuId;

    /**
     * SKU规格组合（快照）
     */
    private String specCombination;

    /**
     * 退款数量
     */
    private Integer refundQuantity;

    /**
     * 退款单价（快照）
     */
    private BigDecimal refundPrice;

    /**
     * 退款小计
     */
    private BigDecimal refundSubtotal;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}

















