package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 运费模板DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class ShippingTemplateDTO {

    /**
     * 运费模板名称
     */
    @NotBlank(message = "运费模板名称不能为空")
    private String templateName;

    /**
     * 计算方式（1-按重量，2-按件数，3-按金额）
     */
    private Integer calculationType;

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
     * 默认首重（单位：kg）
     */
    private BigDecimal defaultFirstWeight;

    /**
     * 默认首重价格
     */
    private BigDecimal defaultFirstPrice;

    /**
     * 默认续重（单位：kg）
     */
    private BigDecimal defaultContinueWeight;

    /**
     * 默认续重价格
     */
    private BigDecimal defaultContinuePrice;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 状态（0-禁用，1-启用）
     */
    private Integer status;

    /**
     * 运费规则列表
     */
    private List<ShippingRuleDTO> rules;
}



