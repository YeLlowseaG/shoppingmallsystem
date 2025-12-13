package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品规格值VO
 */
@Data
public class ProductSpecValueVO {
    
    /**
     * 主键ID
     */
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
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}