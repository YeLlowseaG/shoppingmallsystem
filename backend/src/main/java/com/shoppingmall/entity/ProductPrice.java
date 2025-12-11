package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品价格实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
@TableName("product_price")
public class ProductPrice {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 用户等级（普通/VIP/金牌）
     */
    private String userLevel;

    /**
     * 等级价格
     */
    private BigDecimal price;

    /**
     * 最小数量（用于阶梯价格）
     */
    private Integer minQuantity;

    /**
     * 最大数量（NULL表示无上限）
     */
    private Integer maxQuantity;

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
