package com.shoppingmall.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 聚水潭店铺商品资料上传DTO
 *
 * @author ShoppingMall Team
 * @date 2025-01-14
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JushuitanShopItemDTO {

    /**
     * 聚水潭店铺编号（必填）
     */
    @JsonProperty("shop_id")
    private Integer shopId;

    /**
     * ERP商品规格ID（必填）
     */
    @JsonProperty("sku_id")
    private String skuId;

    /**
     * 线上店铺商品规格ID（必填）
     */
    @JsonProperty("shop_sku_id")
    private String shopSkuId;

    /**
     * 线上店铺商品ID（必填）
     */
    @JsonProperty("shop_i_id")
    private String shopIId;

    /**
     * ERP商品ID（可选）
     */
    @JsonProperty("i_id")
    private String iId;

    /**
     * 商品编码（可选）
     */
    @JsonProperty("sku_code")
    private String skuCode;

    /**
     * 原始规格ID（可选）
     */
    @JsonProperty("original_sku_id")
    private String originalSkuId;

    /**
     * 商品名称（可选，可更新）
     */
    private String name;

    /**
     * 店铺颜色规格（可选，可更新）
     */
    @JsonProperty("shop_properties_value")
    private String shopPropertiesValue;

    /**
     * 商品标识（可选，可更新，可为空字符串）
     */
    @JsonProperty("sku_sign")
    private String skuSign;

    /**
     * 商品线上链接（可选）
     */
    @JsonProperty("shop_sku_url")
    private String shopSkuUrl;
}




