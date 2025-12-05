package com.shoppingmall.dto;

import lombok.Data;

/**
 * 用户信息DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class UserInfoDTO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别（0-女，1-男）
     */
    private Integer gender;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 用户等级
     */
    private String userLevel;

    /**
     * 状态
     */
    private String status;
}


