package com.shoppingmall.service.payment;

import com.shoppingmall.entity.PaymentApiLog;

/**
 * 支付接口日志服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-30
 */
public interface PaymentLogService {

    /**
     * 保存支付接口日志
     *
     * @param log 支付接口日志实体
     */
    void savePaymentLog(PaymentApiLog log);

    /**
     * 更新支付接口日志状态
     *
     * @param orderNo 订单号
     * @param apiType 接口类型
     * @param paymentMethod 支付方式
     * @param apiStatus 接口调用状态（0-失败，1-成功，2-处理中）
     * @param externalTradeNo 外部交易号（可选）
     * @param responseData 响应数据（可选）
     */
    void updatePaymentLogStatus(String orderNo, String apiType, String paymentMethod, Integer apiStatus, String externalTradeNo, String responseData);
}

