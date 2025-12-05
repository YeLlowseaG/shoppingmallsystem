package com.shoppingmall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * 用户注册DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
public class RegisterDTO {

    /**
     * 用户名（必填）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 电子邮箱（必填）
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    /**
     * 密码（必填）
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 确认密码（必填）
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;

    /**
     * 真实姓名（必填）
     */
    @NotBlank(message = "姓名不能为空")
    private String realName;

    /**
     * 性别（必填，0-女，1-男）
     */
    @NotNull(message = "性别不能为空")
    private Integer gender;

    /**
     * 省份（必填）
     */
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 城市（必填）
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 区县（必填）
     */
    @NotBlank(message = "区县不能为空")
    private String district;

    /**
     * 详细地址（必填）
     */
    @NotBlank(message = "联系地址不能为空")
    private String address;

    /**
     * 手机号（必填）
     */
    @NotBlank(message = "移动电话不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 运营人员（必填）
     */
    @NotBlank(message = "运营人员不能为空")
    private String operator;

    /**
     * 验证码ID（必填）
     */
    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    /**
     * 验证码（必填）
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 出生年份（可选）
     */
    private Integer birthYear;

    /**
     * 出生月份（可选）
     */
    private Integer birthMonth;

    /**
     * 出生日期（可选）
     */
    private Integer birthDay;

    /**
     * 邮编（可选）
     */
    private String zipCode;

    /**
     * 固定电话（可选）
     */
    private String fixedPhone;

    /**
     * 安全问题（可选）
     */
    private String securityQuestion;

    /**
     * 安全问题答案（可选）
     */
    private String securityAnswer;

    /**
     * 旺旺账号（可选）
     */
    private String wangwang;
}

