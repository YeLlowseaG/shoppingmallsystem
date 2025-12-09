package com.shoppingmall.dto;

import lombok.Data;

import java.time.LocalDate;

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
     * 用户等级（0-普通，1-VIP，2-金牌）
     */
    private Integer userLevel;

    /**
     * 状态（0-待审核，1-已激活，2-已禁用）
     */
    private Integer status;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 固定电话
     */
    private String fixedPhone;

    /**
     * 安全问题
     */
    private String securityQuestion;

    /**
     * 安全问题答案
     */
    private String securityAnswer;

    /**
     * 旺旺账号
     */
    private String wangwang;

    /**
     * 运营人员
     */
    private String operator;
}


