package com.shoppingmall.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 创建订单DTO
 *
 * @author ShoppingMall Team
 * @date 2025-12-09
 */
@Data
public class CreateOrderDTO {

    /**
     * 收货地址ID
     */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /**
     * 购物车ID列表（从购物车创建订单时使用）
     */
    private List<Long> cartIds;

    /**
     * 商品列表（直接购买时使用）
     */
    private List<OrderItemDTO> items;

    /**
     * 配送方式
     */
    private String shippingMethod;

    /**
     * 配送日期
     */
    private LocalDate deliveryDate;

    /**
     * 配送时间段
     */
    private String deliveryTime;

    /**
     * 支付方式（ALIPAY-支付宝，WECHAT-微信，PRE_DEPOSIT-预存款，OFFLINE-线下支付）
     */
    @NotBlank(message = "支付方式不能为空")
    private String paymentMethod;

    /**
     * 订单备注
     */
    private String orderRemark;

    /**
     * 订单商品DTO
     */
    @Data
    public static class OrderItemDTO {
        /**
         * 商品ID
         */
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        /**
         * SKU ID（可为空，表示无规格或未选择）
         */
        private Long skuId;

        /**
         * 数量
         */
        @NotNull(message = "数量不能为空")
        private Integer quantity;
    }
}























































