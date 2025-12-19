package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 运费模板实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
@TableName("shipping_template")
public class ShippingTemplate {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运费模板名称
     */
    private String templateName;

    /**
     * 计算方式（1-按重量，2-按件数，3-按金额）
     */
    private Integer calculationType;

    /**
     * 包邮金额（订单金额达到此金额时免运费）
     */
    private BigDecimal freeShippingAmount;

    /**
     * 包邮重量（订单重量达到此重量时免运费，单位：kg）
     */
    private BigDecimal freeShippingWeight;

    /**
     * 包邮件数（订单件数达到此数量时免运费）
     */
    private Integer freeShippingQuantity;

    /**
     * 默认首重（单位：kg）
     */
    private BigDecimal defaultFirstWeight;

    /**
     * 默认首重价格
     */
    private BigDecimal defaultFirstPrice;

    /**
     * 默认续重（单位：kg）
     */
    private BigDecimal defaultContinueWeight;

    /**
     * 默认续重价格
     */
    private BigDecimal defaultContinuePrice;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

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
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}


































