package com.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费计算DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class ShippingFeeCalculateDTO {

    /**
     * 配送方式ID
     */
    private Long shippingMethodId;

    /**
     * 订单总重量（单位：kg）
     */
    private BigDecimal totalWeight;

    /**
     * 订单总件数
     */
    private Integer totalQuantity;

    /**
     * 订单总金额
     */
    private BigDecimal totalAmount;

    /**
     * 收货地址省份
     */
    private String province;

    /**
     * 收货地址城市
     */
    private String city;

    /**
     * 收货地址区县
     */
    private String district;
}







































































