package com.shoppingmall.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 聚水潭订单DTO（用于推送订单到聚水潭）
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class JushuitanOrderDTO {

    /**
     * 订单号（必填）
     */
    private String soId;

    /**
     * 店铺编号
     */
    private String shopId;

    /**
     * 下单时间（yyyy-MM-dd HH:mm:ss）
     */
    private String orderDate;

    /**
     * 付款时间（yyyy-MM-dd HH:mm:ss）
     */
    private String payDate;

    /**
     * 收货人姓名
     */
    private String receiverName;

    /**
     * 收货人手机
     */
    private String receiverMobile;

    /**
     * 省
     */
    private String receiverProvince;

    /**
     * 市
     */
    private String receiverCity;

    /**
     * 区
     */
    private String receiverDistrict;

    /**
     * 详细地址
     */
    private String receiverAddress;

    /**
     * 买家留言
     */
    private String buyerMessage;

    /**
     * 卖家备注
     */
    private String sellerMemo;

    /**
     * 运费
     */
    private BigDecimal freight;

    /**
     * 应付金额
     */
    private BigDecimal payAmount;

    /**
     * 商品明细
     */
    private List<Item> items;

    /**
     * 商品明细
     */
    @Data
    public static class Item {
        /**
         * 商品编码（SKU）
         */
        private String skuId;

        /**
         * 商品名称
         */
        private String itemName;

        /**
         * 数量
         */
        private Integer qty;

        /**
         * 单价
         */
        private BigDecimal price;
    }
}
