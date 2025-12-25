package com.shoppingmall.vo;

import lombok.Data;

import java.util.List;

/**
 * 管理员登录响应VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class AdminLoginVO {
    /**
     * Token
     */
    private String token;

    /**
     * 管理员信息
     */
    private AdminInfoVO adminInfo;

    /**
     * 菜单列表
     */
    private List<MenuVO> menus;

    /**
     * 权限列表
     */
    private List<String> permissions;
}














































