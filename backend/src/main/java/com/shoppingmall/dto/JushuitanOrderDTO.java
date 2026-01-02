package com.shoppingmall.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("so_id")
    private String soId;

    /**
     * 店铺编号
     */
    @JsonProperty("shop_id")
    private String shopId;

    /**
     * 下单时间（yyyy-MM-dd HH:mm:ss）
     */
    @JsonProperty("order_date")
    private String orderDate;

    /**
     * 付款时间（yyyy-MM-dd HH:mm:ss）
     */
    @JsonProperty("pay_date")
    private String payDate;

    /**
     * 收货人姓名
     */
    @JsonProperty("receiver_name")
    private String receiverName;

    /**
     * 收货人手机
     */
    @JsonProperty("receiver_mobile")
    private String receiverMobile;

    /**
     * 省
     */
    @JsonProperty("receiver_province")
    private String receiverProvince;

    /**
     * 市
     */
    @JsonProperty("receiver_city")
    private String receiverCity;

    /**
     * 区
     */
    @JsonProperty("receiver_district")
    private String receiverDistrict;

    /**
     * 详细地址
     */
    @JsonProperty("receiver_address")
    private String receiverAddress;

    /**
     * 买家留言
     */
    @JsonProperty("buyer_message")
    private String buyerMessage;

    /**
     * 卖家备注
     */
    @JsonProperty("seller_memo")
    private String sellerMemo;

    /**
     * 运费
     */
    @JsonProperty("freight")
    private BigDecimal freight;

    /**
     * 应付金额
     */
    @JsonProperty("pay_amount")
    private BigDecimal payAmount;

    /**
     * 支付信息（必填）
     */
    private List<Pay> pay;

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
        @JsonProperty("sku_id")
        private String skuId;

        /**
         * 商品名称
         */
        @JsonProperty("item_name")
        private String itemName;

        /**
         * 数量
         */
        @JsonProperty("qty")
        private Integer qty;

        /**
         * 单价
         */
        @JsonProperty("price")
        private BigDecimal price;
    }

    /**
     * 支付信息
     */
    @Data
    public static class Pay {
        /**
         * 外部支付单号
         */
        @JsonProperty("outer_pay_id")
        private String outerPayId;

        /**
         * 支付时间（yyyy-MM-dd HH:mm:ss）
         */
        @JsonProperty("pay_date")
        private String payDate;

        /**
         * 支付方式
         */
        @JsonProperty("payment")
        private String payment;

        /**
         * 卖家账号
         */
        @JsonProperty("seller_account")
        private String sellerAccount;

        /**
         * 买家账号
         */
        @JsonProperty("buyer_account")
        private String buyerAccount;

        /**
         * 实付金额（必填，需要等于订单的 pay_amount）
         */
        @JsonProperty("amount")
        private BigDecimal amount;
    }
}
