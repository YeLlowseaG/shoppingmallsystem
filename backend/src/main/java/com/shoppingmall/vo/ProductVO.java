package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Data
public class ProductVO {

    /**
     * 商品ID
     */
    private Long id;

    /**
     * 商品编码/SKU
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 分类名称
     */
    private String categoryName;

    /**
     * 品牌ID
     */
    private Long brandId;

    /**
     * 品牌名称
     */
    private String brandName;

    /**
     * 主图URL
     */
    private String mainImage;

    /**
     * 商品图片列表
     */
    private List<String> imageList;

    /**
     * 商品描述
     */
    private String description;

    /**
     * 基础批发价
     */
    private BigDecimal basePrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 市场价格
     */
    private BigDecimal marketPrice;

    /**
     * 成本价格
     */
    private BigDecimal costPrice;

    /**
     * 用户等级价格（根据当前登录用户的等级）
     */
    private BigDecimal userLevelPrice;

    /**
     * 库存数量
     */
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
     * 销量
     */
    private Integer salesCount;

    /**
     * 状态（上架/下架）
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
