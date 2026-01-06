package com.shoppingmall.payment.config;

import lombok.Data;

/**
 * 微信支付配置实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class WeChatPayConfig {

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 环境：sandbox/production
     */
    private String env;

    /**
     * 沙箱环境配置
     */
    private WeChatPayEnvConfig sandbox;

    /**
     * 生产环境配置
     */
    private WeChatPayEnvConfig production;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 微信支付环境配置
     */
    @Data
    public static class WeChatPayEnvConfig {
        /**
         * AppID
         */
        private String appid;

        /**
         * 商户号
         */
        private String mchid;

        /**
         * API密钥
         */
        private String key;

        /**
         * 证书路径
         */
        private String certPath;
    }
}


























