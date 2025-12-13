package com.shoppingmall.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
public class ProductDTO {

    /**
     * 商品ID（编辑时使用）
     */
    private Long id;

    /**
     * 商品编码/SKU
     */
    @NotBlank(message = "商品编码不能为空")
    @Size(max = 50, message = "商品编码长度不能超过50个字符")
    private String productCode;

    /**
     * 商品名称
     */
    @NotBlank(message = "商品名称不能为空")
    @Size(max = 200, message = "商品名称长度不能超过200个字符")
    private String productName;

    /**
     * 分类ID
     */
    @NotNull(message = "分类不能为空")
    private Long categoryId;

    /**
     * 品牌ID（非必填）
     */
    private Long brandId;

    /**
     * 主图URL
     */
    private String mainImage;

    /**
     * 商品图片（JSON数组字符串）
     */
    private String images;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 销售价格
     */
    @NotNull(message = "价格不能为空")
    @DecimalMin(value = "0.01", message = "价格必须大于0")
    private BigDecimal basePrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 库存数量
     */
    @NotNull(message = "库存不能为空")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    /**
     * 警戒库存
     */
    private Integer warningStock;

    /**
     * 商品重量(g)
     */
    private BigDecimal weight;

    /**
     * 状态（上架/下架）
     */
    private String status;
}
