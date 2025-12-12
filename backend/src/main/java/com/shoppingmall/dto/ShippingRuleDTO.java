package com.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 运费规则DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class ShippingRuleDTO {

    /**
     * 运费规则ID（更新时使用）
     */
    private Long id;

    /**
     * 地区编码（省/市/区，为空表示默认规则）
     */
    private String regionCode;

    /**
     * 地区名称（省/市/区，为空表示默认规则）
     */
    private String regionName;

    /**
     * 首重（单位：kg，按件数时表示首件）
     */
    private BigDecimal firstWeight;

    /**
     * 首重价格（按件数时表示首件价格）
     */
    private BigDecimal firstPrice;

    /**
     * 续重（单位：kg，按件数时表示续件）
     */
    private BigDecimal continueWeight;

    /**
     * 续重价格（按件数时表示续件价格）
     */
    private BigDecimal continuePrice;

    /**
     * 包邮金额
     */
    private BigDecimal freeShippingAmount;

    /**
     * 包邮重量（单位：kg）
     */
    private BigDecimal freeShippingWeight;

    /**
     * 包邮件数
     */
    private Integer freeShippingQuantity;

    /**
     * 排序
     */
    private Integer sortOrder;
}





