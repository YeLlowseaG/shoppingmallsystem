package com.shoppingmall.dto;

import lombok.Data;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * 商品SKU DTO
 */
@Data
public class ProductSkuDTO {
    
    /**
     * 商品ID
     */
    @NotNull(message = "商品ID不能为空")
    private Long productId;
    
    /**
     * SKU编码（唯一）
     */
    @NotBlank(message = "SKU编码不能为空")
    @Size(max = 100, message = "SKU编码长度不能超过100个字符")
    private String skuCode;
    
    /**
     * 规格组合JSON（如：{"颜色":"红色","尺寸":"L"}）
     */
    @NotBlank(message = "规格组合不能为空")
    private String specCombination;
    
    /**
     * SKU价格（基础价）
     */
    @NotNull(message = "SKU价格不能为空")
    @DecimalMin(value = "0.01", message = "SKU价格必须大于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal price;

    /**
     * 建议零售价
     */
    @DecimalMin(value = "0", message = "建议零售价不能小于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal suggestedRetailPrice;

    /**
     * 市场零售价
     */
    @DecimalMin(value = "0", message = "市场零售价不能小于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal marketRetailPrice;

    /**
     * 会员价（启用时作为售价）
     */
    @DecimalMin(value = "0", message = "会员价不能小于0")
    @Digits(integer = 8, fraction = 2, message = "价格格式不正确")
    private BigDecimal memberPrice;

    /**
     * 是否启用会员价（0-否，1-是）
     */
    private Integer enableMemberPrice;

    /**
     * SKU库存
     */
    @NotNull(message = "SKU库存不能为空")
    @Min(value = 0, message = "库存不能小于0")
    private Integer stock;
    
    /**
     * SKU警戒库存
     */
    @Min(value = 0, message = "警戒库存不能小于0")
    private Integer warningStock;
    
    /**
     * SKU重量(g)
     */
    @DecimalMin(value = "0", message = "重量不能小于0")
    private BigDecimal weight;
    
    /**
     * SKU主图（可选）
     */
    @Size(max = 500, message = "SKU主图URL长度不能超过500个字符")
    private String skuImage;
    
    /**
     * SKU图片列表（可选）
     */
    private String skuImages;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status = 1;

    /**
     * SKU会员价列表（按会员等级设置）
     */
    private java.util.List<ProductSkuMemberPriceDTO> memberPrices;
}