package com.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 商品导入DTO（对应CSV的一行数据）
 */
@Data
public class ProductImportDTO {
    
    private Integer rowNumber;
    
    private String productCode;
    
    private String barcode;
    
    private String unit;
    
    private String productName;
    
    private String categoryName;
    
    private String brandName;

    private Long shippingTemplateId;

    private BigDecimal basePrice;
    
    private BigDecimal suggestedRetailPrice;
    
    private BigDecimal marketRetailPrice;
    
    private Integer warningStock;
    
    private BigDecimal weight;
    
    private String description;
    
    private String status;
    
    private Boolean enableMemberPrice; // 是否启用商品会员价
    
    private Map<String, BigDecimal> productMemberPrices = new HashMap<>(); // 商品会员价（key: 会员等级名称, value: 会员价）
    
    private Boolean enableSpec;
    
    private String skuCode;
    
    private String specCombination;
    
    private BigDecimal skuPrice;
    
    private Integer skuStock;
    
    private Boolean enableSkuMemberPrice; // 是否启用SKU会员价
    
    private Map<String, BigDecimal> skuMemberPrices = new HashMap<>(); // SKU会员价（key: 会员等级名称, value: 会员价）
    
    // 以下字段已废弃，保留用于兼容旧模板
    @Deprecated
    private BigDecimal skuMemberPrice;
}
