package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 帮助中心文章DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class HelpArticleDTO {

    /**
     * 主键ID（新增时不需要，更新时需要）
     */
    private Long id;

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    private String title;

    /**
     * 文章内容（HTML格式，支持富文本和图片）
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;

    /**
     * 图片URL数组
     */
    private List<String> images;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort = 0;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status = 1;
}

