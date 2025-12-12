package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导航菜单实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Data
@TableName("navigation_menu")
public class NavigationMenu {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单链接
     */
    private String menuUrl;

    /**
     * 菜单类型（link-直接链接，category-分类，brand-品牌，type-类型）
     */
    private String menuType;

    /**
     * 菜单参数，JSON格式存储
     */
    private String menuParams;

    /**
     * 菜单图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 打开方式（_self-当前窗口，_blank-新窗口）
     */
    private String target;

    /**
     * 菜单描述
     */
    private String description;

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