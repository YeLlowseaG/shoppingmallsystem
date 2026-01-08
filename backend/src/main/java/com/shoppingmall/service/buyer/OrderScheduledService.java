package com.shoppingmall.service.buyer;

/**
 * 订单定时任务服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
public interface OrderScheduledService {

    /**
     * 自动取消超时的待付款订单
     * 将超过指定时间未支付的订单自动取消并恢复库存
     */
    void cancelTimeoutOrders();
}






































































