package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 帮助中心分类DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class HelpCategoryDTO {

    /**
     * 主键ID（新增时不需要，更新时需要）
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
    private String name;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort = 0;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status = 1;
}

