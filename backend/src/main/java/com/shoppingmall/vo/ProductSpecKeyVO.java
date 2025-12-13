package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品规格属性VO
 */
@Data
public class ProductSpecKeyVO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 商品ID
     */
    private Long productId;
    
    /**
     * 规格名称（如：颜色、尺寸、容量）
     */
    private String specName;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
    /**
     * 规格值列表
     */
    private List<ProductSpecValueVO> specValues;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}