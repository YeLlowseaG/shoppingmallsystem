package com.shoppingmall.common.constant;

/**
 * 支付状态常量
 *
 * @author ShoppingMall Team
 * @date 2025-12-05
 */
public class PaymentStatus {
    /**
     * 未支付/待支付
     */
    public static final Integer UNPAID = 0;

    /**
     * 已支付
     */
    public static final Integer PAID = 1;

    /**
     * 已退款
     */
    public static final Integer REFUNDED = 2;

    /**
     * 已失败
     */
    public static final Integer FAILED = 3;
}

