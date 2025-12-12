package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
@TableName("product")
public class Product {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商品编码/SKU（唯一）
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 主图URL
     */
    private String mainImage;

    /**
     * 商品图片（JSON数组）
     */
    private String images;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 基础批发价
     */
    private BigDecimal basePrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    private Integer stock;

    /**
     * 警戒库存
     */
    private Integer warningStock;

    /**
     * 商品重量(g)
     */
    private Integer weight;

    /**
     * 销量
     */
    private Integer salesCount;

    /**
     * 状态（0-下架，1-上架）
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
