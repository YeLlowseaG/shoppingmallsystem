package com.shoppingmall.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 聚水潭商品上传DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-31
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JushuitanItemDTO {

    /**
     * 商品编码（必填）
     */
    @JsonProperty("sku_id")
    private String skuId;

    /**
     * 款式编码（必填）
     */
    @JsonProperty("i_id")
    private String iId;

    /**
     * 商品名称（必填）
     */
    private String name;

    /**
     * 基本售价（聚水潭字段名：s_price）
     */
    @JsonProperty("s_price")
    private BigDecimal sPrice;

    /**
     * 成本价
     */
    @JsonProperty("cost_price")
    private BigDecimal costPrice;

    /**
     * 市场价/吊牌价
     */
    @JsonProperty("market_price")
    private BigDecimal marketPrice;

    /**
     * 重量（克）
     */
    private BigDecimal weight;

    /**
     * 长（厘米）
     */
    private BigDecimal l;

    /**
     * 宽（厘米）
     */
    private BigDecimal w;

    /**
     * 高（厘米）
     */
    private BigDecimal h;

    /**
     * 商品图片URL
     */
    private String pic;

    /**
     * 大图URL
     */
    @JsonProperty("pic_big")
    private String picBig;

    /**
     * SKU图片URL
     */
    @JsonProperty("sku_pic")
    private String skuPic;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 商品分类
     */
    @JsonProperty("c_name")
    private String cName;

    /**
     * 虚拟分类
     */
    @JsonProperty("vc_name")
    private String vcName;

    /**
     * 商品属性（成品/半成品/原材料/包材）
     */
    @JsonProperty("item_type")
    private String itemType;

    /**
     * 颜色及规格（如：蓝色;XXL）
     */
    @JsonProperty("properties_value")
    private String propertiesValue;

    /**
     * 简称
     */
    @JsonProperty("short_name")
    private String shortName;

    /**
     * 备注
     */
    private String remark;

    /**
     * 国标码/条形码
     */
    @JsonProperty("sku_code")
    private String skuCode;

    /**
     * 计量单位
     */
    private String unit;

    /**
     * 是否启用（1=启用, -1=禁用, 0=备用）
     */
    private Integer enabled;

    /**
     * 禁止库存同步（true=禁止，false=允许）
     */
    @JsonProperty("stock_disabled")
    private Boolean stockDisabled;

    /**
     * 供应商名称
     */
    @JsonProperty("supplier_name")
    private String supplierName;

    /**
     * 供应商商品编码
     */
    @JsonProperty("supplier_sku_id")
    private String supplierSkuId;

    /**
     * 供应商款式编码
     */
    @JsonProperty("supplier_i_id")
    private String supplierIId;
}
