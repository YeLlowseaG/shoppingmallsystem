package com.shoppingmall.dto;

import lombok.Data;

/**
 * 地区信息DTO（用于运费规则中的多地区存储）
 *
 * @author ShoppingMall Team
 * @date 2025-12-27
 */
@Data
public class RegionInfoDTO {

    /**
     * 省份编码
     */
    private String provinceCode;

    /**
     * 省份名称
     */
    private String provinceName;

    /**
     * 城市编码（可选）
     */
    private String cityCode;

    /**
     * 城市名称（可选）
     */
    private String cityName;

    /**
     * 区县编码（可选）
     */
    private String districtCode;

    /**
     * 区县名称（可选）
     */
    private String districtName;
}

