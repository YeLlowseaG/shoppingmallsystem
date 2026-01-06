package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 配送方式实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
@TableName("shipping_method")
public class ShippingMethod {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 配送方式编码（唯一）
     */
    private String methodCode;

    /**
     * 配送方式名称
     */
    private String methodName;

    /**
     * 关联物流公司ID（可为空，如上门自提）
     */
    private Long logisticsCompanyId;

    /**
     * 配送方式描述
     */
    private String description;

    /**
     * 关联运费模板ID
     */
    private Long shippingTemplateId;

    /**
     * 基础运费（固定运费时使用）
     */
    private BigDecimal basePrice;

    /**
     * 运费计算方式（1-固定运费，2-按重量，3-按件数，4-按金额，5-运费模板）
     */
    private Integer calculationType;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sortOrder;

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



































































