package com.shoppingmall.payment.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.entity.SystemConfig;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 支付配置服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentConfigServiceImpl implements PaymentConfigService {

    private final SystemConfigService systemConfigService;
    
    @Autowired
    private Cache<String, Object> localCache;

    private static final String WECHAT_CONFIG_CACHE_KEY = "payment:wechat:config";
    private static final String ALIPAY_CONFIG_CACHE_KEY = "payment:alipay:config";
    private static final String WECHAT_ENABLED_CACHE_KEY = "payment:wechat:enabled";
    private static final String ALIPAY_ENABLED_CACHE_KEY = "payment:alipay:enabled";

    @Override
    public WeChatPayConfig getWeChatPayConfig() {
        return (WeChatPayConfig) localCache.get(WECHAT_CONFIG_CACHE_KEY, key -> {
            log.debug("从数据库加载微信支付配置");
            return buildWeChatPayConfig();
        });
    }

    @Override
    public AlipayConfig getAlipayConfig() {
        return (AlipayConfig) localCache.get(ALIPAY_CONFIG_CACHE_KEY, key -> {
            log.debug("从数据库加载支付宝配置");
            return buildAlipayConfig();
        });
    }

    @Override
    public boolean isPaymentEnabled(String paymentMethod) {
        String cacheKey = "WECHAT".equals(paymentMethod) ? WECHAT_ENABLED_CACHE_KEY : ALIPAY_ENABLED_CACHE_KEY;
        return (Boolean) localCache.get(cacheKey, key -> {
            String enabledKey = "WECHAT".equals(paymentMethod) 
                    ? "payment.wechat.enabled" 
                    : "payment.alipay.enabled";
            String enabled = systemConfigService.getConfigValue(enabledKey, "0");
            return "1".equals(enabled);
        });
    }

    @Override
    public void refreshConfig() {
        log.info("刷新支付配置缓存");
        localCache.invalidate(WECHAT_CONFIG_CACHE_KEY);
        localCache.invalidate(ALIPAY_CONFIG_CACHE_KEY);
        localCache.invalidate(WECHAT_ENABLED_CACHE_KEY);
        localCache.invalidate(ALIPAY_ENABLED_CACHE_KEY);
    }

    /**
     * 构建微信支付配置
     */
    private WeChatPayConfig buildWeChatPayConfig() {
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix("payment.wechat.");
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : "",
                        (existing, replacement) -> existing
                ));

        WeChatPayConfig config = new WeChatPayConfig();
        
        // 基本配置
        String enabled = configMap.getOrDefault("payment.wechat.enabled", "0");
        config.setEnabled("1".equals(enabled));
        
        String env = configMap.getOrDefault("payment.wechat.env", "sandbox");
        config.setEnv(env);
        
        config.setNotifyUrl(configMap.getOrDefault("payment.wechat.notify_url", ""));

        // 沙箱环境配置
        WeChatPayConfig.WeChatPayEnvConfig sandbox = new WeChatPayConfig.WeChatPayEnvConfig();
        sandbox.setAppid(configMap.getOrDefault("payment.wechat.sandbox.appid", ""));
        sandbox.setMchid(configMap.getOrDefault("payment.wechat.sandbox.mchid", ""));
        sandbox.setKey(configMap.getOrDefault("payment.wechat.sandbox.key", ""));
        sandbox.setCertPath(configMap.getOrDefault("payment.wechat.sandbox.cert_path", ""));
        config.setSandbox(sandbox);

        // 生产环境配置
        WeChatPayConfig.WeChatPayEnvConfig production = new WeChatPayConfig.WeChatPayEnvConfig();
        production.setAppid(configMap.getOrDefault("payment.wechat.production.appid", ""));
        production.setMchid(configMap.getOrDefault("payment.wechat.production.mchid", ""));
        production.setKey(configMap.getOrDefault("payment.wechat.production.key", ""));
        production.setCertPath(configMap.getOrDefault("payment.wechat.production.cert_path", ""));
        config.setProduction(production);

        return config;
    }

    /**
     * 构建支付宝配置
     */
    private AlipayConfig buildAlipayConfig() {
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix("payment.alipay.");
        Map<String, String> configMap = configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : "",
                        (existing, replacement) -> existing
                ));

        AlipayConfig config = new AlipayConfig();
        
        // 基本配置
        String enabled = configMap.getOrDefault("payment.alipay.enabled", "0");
        config.setEnabled("1".equals(enabled));
        
        String env = configMap.getOrDefault("payment.alipay.env", "sandbox");
        config.setEnv(env);
        
        config.setNotifyUrl(configMap.getOrDefault("payment.alipay.notify_url", ""));

        // 沙箱环境配置
        AlipayConfig.AlipayEnvConfig sandbox = new AlipayConfig.AlipayEnvConfig();
        sandbox.setAppid(configMap.getOrDefault("payment.alipay.sandbox.appid", ""));
        sandbox.setPrivateKey(configMap.getOrDefault("payment.alipay.sandbox.private_key", ""));
        sandbox.setPublicKey(configMap.getOrDefault("payment.alipay.sandbox.public_key", ""));
        config.setSandbox(sandbox);

        // 生产环境配置
        AlipayConfig.AlipayEnvConfig production = new AlipayConfig.AlipayEnvConfig();
        production.setAppid(configMap.getOrDefault("payment.alipay.production.appid", ""));
        production.setPrivateKey(configMap.getOrDefault("payment.alipay.production.private_key", ""));
        production.setPublicKey(configMap.getOrDefault("payment.alipay.production.public_key", ""));
        config.setProduction(production);

        return config;
    }

    @Override
    public void updateWeChatPayConfig(WeChatPayConfig config) {
        log.info("更新微信支付配置");
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix("payment.wechat.");
        Map<String, SystemConfig> configMap = configs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, configItem -> configItem));

        // 更新基本配置
        updateConfigValue(configMap, "payment.wechat.enabled", config.getEnabled() != null && config.getEnabled() ? "1" : "0");
        if (config.getEnv() != null) {
            updateConfigValue(configMap, "payment.wechat.env", config.getEnv());
        }
        if (config.getNotifyUrl() != null) {
            updateConfigValue(configMap, "payment.wechat.notify_url", config.getNotifyUrl());
        }

        // 更新沙箱环境配置
        if (config.getSandbox() != null) {
            WeChatPayConfig.WeChatPayEnvConfig sandbox = config.getSandbox();
            if (sandbox.getAppid() != null) {
                updateConfigValue(configMap, "payment.wechat.sandbox.appid", sandbox.getAppid());
            }
            if (sandbox.getMchid() != null) {
                updateConfigValue(configMap, "payment.wechat.sandbox.mchid", sandbox.getMchid());
            }
            if (sandbox.getKey() != null) {
                updateConfigValue(configMap, "payment.wechat.sandbox.key", sandbox.getKey());
            }
            if (sandbox.getCertPath() != null) {
                updateConfigValue(configMap, "payment.wechat.sandbox.cert_path", sandbox.getCertPath());
            }
        }

        // 更新生产环境配置
        if (config.getProduction() != null) {
            WeChatPayConfig.WeChatPayEnvConfig production = config.getProduction();
            if (production.getAppid() != null) {
                updateConfigValue(configMap, "payment.wechat.production.appid", production.getAppid());
            }
            if (production.getMchid() != null) {
                updateConfigValue(configMap, "payment.wechat.production.mchid", production.getMchid());
            }
            if (production.getKey() != null) {
                updateConfigValue(configMap, "payment.wechat.production.key", production.getKey());
            }
            if (production.getCertPath() != null) {
                updateConfigValue(configMap, "payment.wechat.production.cert_path", production.getCertPath());
            }
        }

        // 批量更新配置
        systemConfigService.batchUpdateConfigs(configMap.values().stream().collect(Collectors.toList()));

        // 刷新缓存
        refreshConfig();
    }

    @Override
    public void updateAlipayConfig(AlipayConfig config) {
        log.info("更新支付宝配置");
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix("payment.alipay.");
        Map<String, SystemConfig> configMap = configs.stream()
                .collect(Collectors.toMap(SystemConfig::getConfigKey, configItem -> configItem));

        // 更新基本配置
        updateConfigValue(configMap, "payment.alipay.enabled", config.getEnabled() != null && config.getEnabled() ? "1" : "0");
        if (config.getEnv() != null) {
            updateConfigValue(configMap, "payment.alipay.env", config.getEnv());
        }
        if (config.getNotifyUrl() != null) {
            updateConfigValue(configMap, "payment.alipay.notify_url", config.getNotifyUrl());
        }

        // 更新沙箱环境配置
        if (config.getSandbox() != null) {
            AlipayConfig.AlipayEnvConfig sandbox = config.getSandbox();
            if (sandbox.getAppid() != null) {
                updateConfigValue(configMap, "payment.alipay.sandbox.appid", sandbox.getAppid());
            }
            if (sandbox.getPrivateKey() != null) {
                updateConfigValue(configMap, "payment.alipay.sandbox.private_key", sandbox.getPrivateKey());
            }
            if (sandbox.getPublicKey() != null) {
                updateConfigValue(configMap, "payment.alipay.sandbox.public_key", sandbox.getPublicKey());
            }
        }

        // 更新生产环境配置
        if (config.getProduction() != null) {
            AlipayConfig.AlipayEnvConfig production = config.getProduction();
            if (production.getAppid() != null) {
                updateConfigValue(configMap, "payment.alipay.production.appid", production.getAppid());
            }
            if (production.getPrivateKey() != null) {
                updateConfigValue(configMap, "payment.alipay.production.private_key", production.getPrivateKey());
            }
            if (production.getPublicKey() != null) {
                updateConfigValue(configMap, "payment.alipay.production.public_key", production.getPublicKey());
            }
        }

        // 批量更新配置
        systemConfigService.batchUpdateConfigs(configMap.values().stream().collect(Collectors.toList()));

        // 刷新缓存
        refreshConfig();
    }

    /**
     * 更新配置值
     */
    private void updateConfigValue(Map<String, SystemConfig> configMap, String configKey, String configValue) {
        SystemConfig config = configMap.get(configKey);
        if (config != null) {
            config.setConfigValue(configValue);
        } else {
            log.warn("配置项不存在：{}", configKey);
        }
    }
}

