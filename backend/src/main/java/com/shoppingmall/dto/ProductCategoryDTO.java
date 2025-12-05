package com.shoppingmall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 商品分类DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
public class ProductCategoryDTO {

    /**
     * 分类ID（编辑时使用）
     */
    private Long id;

    /**
     * 父分类ID（0表示顶级分类）
     */
    @NotNull(message = "父分类ID不能为空")
    private Long parentId;

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100个字符")
    private String categoryName;

    /**
     * 分类级别（1-一级，2-二级，3-三级）
     */
    @NotNull(message = "分类级别不能为空")
    @Min(value = 1, message = "分类级别最小为1")
    @Max(value = 3, message = "分类级别最大为3")
    private Integer level;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}
