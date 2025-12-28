package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.shoppingmall.vo.ProductSkuVO;

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
     * 条码
     */
    private String barcode;

    /**
     * 计量单位
     */
    private String unit;

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
     * 运费模板ID
     */
    private Long shippingTemplateId;

    /**
     * 运费模板名称
     */
    private String shippingTemplateName;

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
     * 销售价格
     */
    private BigDecimal basePrice;

    /**
     * 销售价格
     */
    private BigDecimal salePrice;

    /**
     * 建议零售价
     */
    private BigDecimal suggestedRetailPrice;

    /**
     * 市场零售价
     */
    private BigDecimal marketRetailPrice;

    /**
     * 用户等级价格（根据当前登录用户的等级）
     */
    private BigDecimal userLevelPrice;

    /**
     * 会员价（根据当前用户会员等级折扣计算，或商品设置的固定会员价）
     */
    private BigDecimal memberPrice;

    /**
     * 是否启用会员价（0-否，1-是）
     */
    private Integer enableMemberPrice;

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
     * 是否启用规格（0-否，1-是）
     */
    private Integer enableSpec;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * SKU 列表
     */
    private List<ProductSkuVO> skus;

    /**
     * 用户是否是会员（0-普通用户，1-会员）
     */
    private Integer isMember;
}
