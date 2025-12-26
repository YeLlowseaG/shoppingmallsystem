package com.shoppingmall.common.constant;

/**
 * 预存款状态常量
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
public class DepositStatus {
    /**
     * 待审核（用于线下充值、代充值等需要管理员审核的场景）
     */
    public static final Integer PENDING_AUDIT = 0;

    /**
     * 已通过（支付成功或审核通过）
     */
    public static final Integer APPROVED = 1;

    /**
     * 已拒绝/支付失败（状态值2）
     * - 线上充值（支付宝/微信）：显示为"支付失败"
     * - 线下充值/代充值：显示为"已拒绝"（审核拒绝）
     */
    public static final Integer REJECTED = 2;

    /**
     * 支付中（用户已发起支付，等待支付完成）
     * 适用于：在线充值（支付宝、微信等）
     */
    public static final Integer PAYING = 3;

    /**
     * 已超时（支付超时未完成）
     */
    public static final Integer TIMEOUT = 4;
}

