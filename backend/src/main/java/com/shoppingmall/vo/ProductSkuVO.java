package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品SKU VO
 */
@Data
public class ProductSkuVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * SKU编码（唯一）
     */
    private String skuCode;

    /**
     * 规格组合JSON（如：{"颜色":"红色","尺寸":"L"}）
     */
    private String specCombination;

    /**
     * SKU价格
     */
    private BigDecimal price;

    /**
     * SKU 会员价（根据当前用户会员等级折扣计算）
     */
    private BigDecimal memberPrice;

    /**
     * SKU库存
     */
    private Integer stock;

    /**
     * SKU警戒库存
     */
    private Integer warningStock;

    /**
     * SKU销量
     */
    private Integer salesCount;

    /**
     * SKU重量(g)
     */
    private BigDecimal weight;

    /**
     * SKU主图（可选）
     */
    private String skuImage;

    /**
     * SKU图片列表（可选）
     */
    private String skuImages;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}