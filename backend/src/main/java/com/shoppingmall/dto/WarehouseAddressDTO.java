package com.shoppingmall.dto;

import lombok.Data;

/**
 * 发货地址库DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Data
public class WarehouseAddressDTO {

    /**
     * 仓库名称
     */
    private String warehouseName;

    /**
     * 联系人姓名
     */
    private String contactName;

    /**
     * 联系电话
     */
    private String contactPhone;

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
    private String detailAddress;

    /**
     * 邮编
     */
    private String zipCode;

    /**
     * 是否默认（0-否，1-是）
     */
    private Integer isDefault;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;
}

