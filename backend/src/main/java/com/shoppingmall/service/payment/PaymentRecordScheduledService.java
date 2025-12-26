package com.shoppingmall.service.payment;

/**
 * 支付记录定时任务服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
public interface PaymentRecordScheduledService {

    /**
     * 自动取消超时的支付中支付记录
     * 将超过指定时间仍处于支付中状态的支付记录自动关闭
     */
    void cancelTimeoutPaymentRecords();
}

