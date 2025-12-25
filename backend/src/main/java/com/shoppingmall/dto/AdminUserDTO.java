package com.shoppingmall.dto;

import lombok.Data;

/**
 * 管理员用户DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class AdminUserDTO {
    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}














































