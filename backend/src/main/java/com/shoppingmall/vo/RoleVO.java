package com.shoppingmall.vo;

import lombok.Data;

import java.util.List;

/**
 * 角色VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class RoleVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 菜单ID列表（用于编辑时回显）
     */
    private List<Long> menuIds;
}

