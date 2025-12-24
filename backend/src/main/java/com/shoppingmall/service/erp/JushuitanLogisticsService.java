package com.shoppingmall.service.erp;

/**
 * 聚水潭物流回传服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface JushuitanLogisticsService {

    /**
     * 拉取订单物流信息
     *
     * @param orderId 订单ID
     * @return 是否成功
     */
    boolean pullLogistics(Long orderId);

    /**
     * 批量拉取物流信息
     *
     * @param orderIds 订单ID列表
     * @return 成功拉取的订单数量
     */
    int batchPullLogistics(java.util.List<Long> orderIds);

    /**
     * 拉取所有已同步但未发货的订单物流信息
     *
     * @return 成功拉取的订单数量
     */
    int pullPendingLogistics();

    /**
     * 查询订单物流状态
     *
     * @param orderId 订单ID
     * @return 物流状态描述
     */
    String queryLogisticsStatus(Long orderId);
}
