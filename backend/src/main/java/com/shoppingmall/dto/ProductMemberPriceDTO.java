package com.shoppingmall.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品会员价DTO
 *
 * @author ShoppingMall Team
 * @date 2026-01-08
 */
@Data
public class ProductMemberPriceDTO {

    /**
     * 会员等级ID
     */
    @NotNull(message = "会员等级ID不能为空")
    private Long memberLevelId;

    /**
     * 会员价
     */
    @NotNull(message = "会员价不能为空")
    @DecimalMin(value = "0.01", message = "会员价必须大于0")
    @Digits(integer = 8, fraction = 2, message = "会员价格式不正确")
    private BigDecimal memberPrice;
}









