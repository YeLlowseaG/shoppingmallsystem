package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 采购者视图对象
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
@Data
public class BuyerVO {

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
     * 是否会员（0-普通用户，1-会员）
     */
    private Integer isMember;

    /**
     * 会员等级ID（关联 member_level 表，普通用户为 NULL）
     */
    private Long memberLevelId;

    /**
     * 会员等级名称
     */
    private String memberLevelName;

    /**
     * 状态（0-待审核，1-已激活，2-已禁用）
     */
    private Integer status;

    /**
     * 状态名称
     */
    private String statusName;

    /**
     * 审核状态（0-待审核，1-已通过，2-已拒绝）
     */
    private Integer auditStatus;

    /**
     * 审核状态名称
     */
    private String auditStatusName;

    /**
     * 审核意见
     */
    private String auditComment;

    /**
     * 审核人ID
     */
    private Long auditorId;

    /**
     * 审核时间
     */
    private LocalDateTime auditTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}




