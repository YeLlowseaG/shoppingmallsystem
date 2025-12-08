package com.shoppingmall.vo;

import lombok.Data;

import java.util.List;

/**
 * 菜单VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class MenuVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 父菜单ID
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 菜单类型（0-目录，1-菜单，2-按钮）
     */
    private Integer menuType;

    /**
     * 路由路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 子菜单列表
     */
    private List<MenuVO> children;
}


