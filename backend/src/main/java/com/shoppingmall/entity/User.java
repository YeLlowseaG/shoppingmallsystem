package com.shoppingmall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-04
 */
@Data
@TableName("sys_user")
public class User {

    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码（BCrypt加密）
     */
    private String password;

    /**
     * 支付密码（BCrypt加密）
     */
    private String paymentPassword;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 性别（0-女，1-男）
     */
    private Integer gender;

    /**
     * 出生日期
     */
    private LocalDate birthday;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 固定电话
     */
    private String fixedPhone;

    /**
     * 运营人员
     */
    private String operator;

    /**
     * 地区（JSON格式：{"province":"省","city":"市","district":"区"}）
     */
    private String region;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 邮编
     */
    private String zipCode;

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
     * 用户等级（0-普通，1-VIP，2-金牌）
     */
    private Integer userLevel;

    /**
     * 状态（0-待审核，1-已激活，2-已禁用）
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


