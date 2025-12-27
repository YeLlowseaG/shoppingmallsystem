package com.shoppingmall.vo;

import lombok.Data;

/**
 * 发货地址库VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Data
public class WarehouseAddressVO {

    /**
     * 主键ID
     */
    private Long id;

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

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;

    /**
     * 完整地址（拼接后的地址）
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (province != null) {
            sb.append(province);
        }
        if (city != null) {
            sb.append(city);
        }
        if (district != null) {
            sb.append(district);
        }
        if (detailAddress != null) {
            sb.append(detailAddress);
        }
        return sb.toString();
    }
}

