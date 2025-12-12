package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商品规格值DTO
 */
@Data
public class ProductSpecValueDTO {
    
    /**
     * 规格属性ID
     */
    @NotNull(message = "规格属性ID不能为空")
    private Long specKeyId;
    
    /**
     * 规格值（如：红色、L码、500ml）
     */
    @NotBlank(message = "规格值不能为空")
    @Size(max = 100, message = "规格值长度不能超过100个字符")
    private String specValue;
    
    /**
     * 规格图片URL（可选）
     */
    @Size(max = 500, message = "规格图片URL长度不能超过500个字符")
    private String specImage;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
}