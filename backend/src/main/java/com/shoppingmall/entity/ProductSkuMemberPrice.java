package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SKU会员价实体类
 *
 * @author ShoppingMall Team
 * @date 2026-01-08
 */
@Data
@TableName("product_sku_member_price")
public class ProductSkuMemberPrice {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * SKU ID
     */
    private Long skuId;

    /**
     * 会员等级ID
     */
    private Long memberLevelId;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

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







