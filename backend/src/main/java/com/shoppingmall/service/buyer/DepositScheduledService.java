package com.shoppingmall.service.buyer;

/**
 * 预存款定时任务服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface DepositScheduledService {

    /**
     * 处理超时的充值记录
     * 将超过指定时间未支付的充值记录标记为已拒绝
     */
    void handleTimeoutRecharges();
}

































































