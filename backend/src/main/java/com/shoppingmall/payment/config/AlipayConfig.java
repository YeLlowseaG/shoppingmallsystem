package com.shoppingmall.payment.config;

import lombok.Data;

/**
 * 支付宝配置实体类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Data
public class AlipayConfig {

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
    private AlipayEnvConfig sandbox;

    /**
     * 生产环境配置
     */
    private AlipayEnvConfig production;

    /**
     * 回调地址
     */
    private String notifyUrl;

    /**
     * 支付宝环境配置
     */
    @Data
    public static class AlipayEnvConfig {
        /**
         * AppID
         */
        private String appid;

        /**
         * 环境标识：sandbox 或 production
         */
        private String env;

        /**
         * 网关地址
         */
        private String gateway;

        /**
         * 应用私钥
         */
        private String privateKey;

        /**
         * 支付宝公钥
         */
        private String publicKey;
    }
}








