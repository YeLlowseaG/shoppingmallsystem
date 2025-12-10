package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公告视图对象
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
public class AnnouncementVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 公告标题
     */
    private String title;

    /**
     * 公告内容（HTML格式，支持富文本和图片）
     */
    private String content;

    /**
     * 图片URL数组
     */
    private List<String> images;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}

