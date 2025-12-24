package com.shoppingmall.service.erp;

/**
 * 聚水潭订单推送服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface JushuitanOrderService {

    /**
     * 推送订单到聚水潭
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean pushOrder(Long orderId);

    /**
     * 批量推送订单
     *
     * @param orderIds 订单ID列表
     * @return 成功推送的订单数量
     */
    int batchPushOrders(java.util.List<Long> orderIds);

    /**
     * 重试失败的订单推送
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean retryPushOrder(Long orderId);

    /**
     * 查询订单推送状态
     *
     * @param orderId 订单ID
     * @return 推送状态描述
     */
    String queryPushStatus(Long orderId);
}
