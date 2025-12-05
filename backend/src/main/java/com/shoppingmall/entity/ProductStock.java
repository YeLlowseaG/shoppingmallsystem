package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品库存实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
@TableName("product_stock")
public class ProductStock {

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
     * 可用库存
     */
    private Integer availableStock;

    /**
     * 锁定库存（下单锁定）
     */
    private Integer lockedStock;

    /**
     * 总库存
     */
    private Integer totalStock;

    /**
     * 预警阈值
     */
    private Integer warningThreshold;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
