package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 帮助中心文章VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class HelpArticleVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章内容（HTML格式，支持富文本和图片）
     */
    private String content;

    /**
     * 图片URL数组
     */
    private List<String> images;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

































