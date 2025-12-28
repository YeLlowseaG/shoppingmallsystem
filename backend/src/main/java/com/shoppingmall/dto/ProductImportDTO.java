package com.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;

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
    
    private Boolean enableSpec;
    
    private String skuCode;
    
    private String specCombination;
    
    private BigDecimal skuPrice;
    
    private Integer skuStock;
    
    private BigDecimal skuMemberPrice;
    
    private Boolean enableSkuMemberPrice;
}
