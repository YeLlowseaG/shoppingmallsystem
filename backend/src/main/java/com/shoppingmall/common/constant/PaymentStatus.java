package com.shoppingmall.common.constant;

/**
 * 支付状态常量
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
public class PaymentStatus {
    /**
     * 待支付
     */
    public static final Integer UNPAID = 0;

    /**
     * 支付中（用户已发起支付，等待支付完成）
     */
    public static final Integer PAYING = 1;

    /**
     * 已支付
     */
    public static final Integer PAID = 2;

    /**
     * 已关闭（订单超时未支付被关闭）
     */
    public static final Integer CLOSED = 3;

    /**
     * 已失败
     */
    public static final Integer FAILED = 4;

    /**
     * 已退款（全额退款）
     */
    public static final Integer REFUNDED = 5;
}






























