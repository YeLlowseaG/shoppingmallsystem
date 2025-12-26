package com.shoppingmall.payment.service;

import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.config.WeChatPayConfig;

/**
 * 支付配置服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface PaymentConfigService {

    /**
     * 获取微信支付配置
     *
     * @return 微信支付配置
     */
    WeChatPayConfig getWeChatPayConfig();

    /**
     * 获取支付宝配置
     *
     * @return 支付宝配置
     */
    AlipayConfig getAlipayConfig();

    /**
     * 检查支付方式是否启用
     *
     * @param paymentMethod 支付方式（WECHAT/ALIPAY）
     * @return 是否启用
     */
    boolean isPaymentEnabled(String paymentMethod);

    /**
     * 刷新配置缓存
     */
    void refreshConfig();

    /**
     * 更新微信支付配置
     *
     * @param config 微信支付配置
     */
    void updateWeChatPayConfig(WeChatPayConfig config);

    /**
     * 更新支付宝配置
     *
     * @param config 支付宝配置
     */
    void updateAlipayConfig(AlipayConfig config);
}

