package com.shoppingmall.vo;

import lombok.Data;

/**
 * 订单统计VO
 *
 * @author ShoppingMall Team
 * @date 2025-12-14
 */
@Data
public class OrderStatisticsVO {

    /**
     * 未付款订单数量
     */
    private Long unpaidOrderCount;

    /**
     * 已发货订单数量
     */
    private Long shippedOrderCount;

    /**
     * 已作废订单数量
     */
    private Long cancelledOrderCount;
}














































