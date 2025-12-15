package com.shoppingmall.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 购物车VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class CartVO {

    /**
     * 购物车ID
     */
    private Long id;

    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 商品编码
     */
    private String productCode;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 商品图片
     */
    private String image;

    /**
     * 销售价格
     */
    private BigDecimal salesPrice;

    /**
     * 会员价
     */
    private BigDecimal memberPrice;

    /**
     * 数量
     */
    private Integer quantity;

    /**
     * 商品重量（克）
     */
    private BigDecimal weight;

    /**
     * 规格文本（如：颜色:白色 / 尺寸:L）
     */
    private String specText;

    /**
     * 是否选中（前端使用，不存储到数据库）
     */
    private Boolean selected;
}



