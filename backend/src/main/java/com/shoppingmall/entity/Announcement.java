package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公告实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Data
@TableName("announcement")
public class Announcement {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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
     * 图片URL数组（JSON格式：["url1","url2"]）
     */
    private String images;

    /**
     * 发布日期
     */
    private LocalDate publishDate;

    /**
     * 排序（数字越小越靠前）
     */
    private Integer sort;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 逻辑删除（0-未删除，1-已删除）
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

