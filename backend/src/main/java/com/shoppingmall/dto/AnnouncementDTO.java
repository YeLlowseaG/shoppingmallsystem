package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

/**
 * 公告DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class AnnouncementDTO {

    /**
     * 主键ID（新增时不需要，更新时需要）
     */
    private Long id;

    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    private String title;

    /**
     * 公告内容（HTML格式，支持富文本和图片）
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;

    /**
     * 图片URL数组
     */
    private List<String> images;

    /**
     * 发布日期
     */
    @NotNull(message = "发布日期不能为空")
    private LocalDate publishDate;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort = 0;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status = 1;
}














