package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品规格值实体 (如：红色、L码、500ml)
 */
@Data
@TableName("product_spec_value")
public class ProductSpecValue {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 规格属性ID
     */
    private Long specKeyId;
    
    /**
     * 规格值（如：红色、L码、500ml）
     */
    private String specValue;
    
    /**
     * 规格图片URL（可选）
     */
    private String specImage;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
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