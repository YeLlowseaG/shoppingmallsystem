package com.shoppingmall.vo;

import lombok.Data;

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
     * 用户等级名称
     */
    private String userLevelName;

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


