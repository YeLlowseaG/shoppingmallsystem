package com.shoppingmall.dto;

import lombok.Data;

/**
 * 收货地址DTO（用于JSON序列化/反序列化）
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class ShippingAddressDTO {

    /**
     * 收货人姓名
     */
    private String name;

    /**
     * 收货人电话
     */
    private String phone;

    /**
     * 收货人手机
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
     * 邮编
     */
    private String zipCode;

    /**
     * 完整地址（省份+城市+区县+详细地址）
     */
    private String fullAddress;
}








































