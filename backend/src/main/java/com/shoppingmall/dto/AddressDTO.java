package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 收货地址DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-08
 */
@Data
public class AddressDTO {

    /**
     * 收货人姓名
     */
    @NotBlank(message = "收货人姓名不能为空")
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
    @NotBlank(message = "省份不能为空")
    private String province;

    /**
     * 城市
     */
    @NotBlank(message = "城市不能为空")
    private String city;

    /**
     * 区县
     */
    @NotBlank(message = "区县不能为空")
    private String district;

    /**
     * 详细地址
     */
    @NotBlank(message = "详细地址不能为空")
    private String address;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否默认地址
     */
    private Boolean isDefault;
}












































