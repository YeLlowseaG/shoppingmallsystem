package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU实体 (存储具体的商品规格组合)
 */
@Data
@TableName("product_sku")
public class ProductSku {
    
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
     * SKU编码（唯一）
     */
    private String skuCode;
    
    /**
     * 规格组合JSON（如：{"颜色":"红色","尺寸":"L"}）
     */
    private String specCombination;
    
    /**
     * SKU价格
     */
    private BigDecimal price;
    
    /**
     * SKU库存
     */
    private Integer stock;
    
    /**
     * SKU警戒库存
     */
    private Integer warningStock;
    
    /**
     * SKU销量
     */
    private Integer salesCount;
    
    /**
     * SKU重量(g)
     */
    private BigDecimal weight;
    
    /**
     * SKU主图（可选）
     */
    private String skuImage;
    
    /**
     * SKU图片列表（可选）
     */
    private String skuImages;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
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