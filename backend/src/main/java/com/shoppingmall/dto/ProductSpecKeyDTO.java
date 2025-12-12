package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 商品规格属性DTO
 */
@Data
public class ProductSpecKeyDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    /**
     * 规格名称（如：颜色、尺寸、容量）
     */
    @NotBlank(message = "规格名称不能为空")
    @Size(max = 50, message = "规格名称长度不能超过50个字符")
    private String specName;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
}