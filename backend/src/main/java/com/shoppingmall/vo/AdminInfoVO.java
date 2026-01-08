package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员信息VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class AdminInfoVO {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

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

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    private String lastLoginIp;

    /**
     * 角色列表
     */
    private List<RoleVO> roles;
}














































































