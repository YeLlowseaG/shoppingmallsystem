package com.shoppingmall.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货地址VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-08
 */
@Data
public class AddressVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收货人姓名
     */
    private String recipient;

    /**
     * 固定电话
     */
    private String phone;

    /**
     * 手机号码
     */
    private String mobile;

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
     * 完整地址（省市区+详细地址）
     */
    private String fullAddress;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否默认地址
     */
    private Boolean isDefault;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}







































































