package com.shoppingmall.notification.service;

/**
 * 通知服务接口
 * 用于发送各类业务通知（企业微信、短信、邮件等）
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
public interface NotificationService {

    /**
     * 发送订单创建通知
     *
     * @param orderNo 订单号
     */
    void sendOrderCreatedNotification(String orderNo);

    /**
     * 发送支付成功通知
     *
     * @param orderNo 订单号
     */
    void sendPaymentSuccessNotification(String orderNo);
}
