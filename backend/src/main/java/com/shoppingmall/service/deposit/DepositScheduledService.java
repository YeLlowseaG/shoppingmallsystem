package com.shoppingmall.service.deposit;

/**
 * 预存款定时任务服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-26
 */
public interface DepositScheduledService {

    /**
     * 自动取消超时的支付中预存款记录
     * 将超过指定时间仍处于支付中状态的预存款记录自动更新为已超时
     */
    void cancelTimeoutDepositRecords();
}

