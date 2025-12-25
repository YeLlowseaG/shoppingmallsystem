package com.shoppingmall.payment.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.payment.config.AlipayConfig;
import com.shoppingmall.payment.config.WeChatPayConfig;
import com.shoppingmall.payment.service.PaymentConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付配置管理控制器（管理后台）
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/payment/config")
@RequiredArgsConstructor
public class PaymentConfigController {

    private final PaymentConfigService paymentConfigService;

    /**
     * 获取支付配置
     */
    @GetMapping
    public Result<Map<String, Object>> getPaymentConfig() {
        try {
            WeChatPayConfig wechatConfig = paymentConfigService.getWeChatPayConfig();
            AlipayConfig alipayConfig = paymentConfigService.getAlipayConfig();

            Map<String, Object> result = new HashMap<>();
            result.put("wechat", wechatConfig);
            result.put("alipay", alipayConfig);

            return Result.success(result);
        } catch (Exception e) {
            log.error("获取支付配置失败", e);
            return Result.error(500, "获取支付配置失败：" + e.getMessage());
        }
    }

    /**
     * 刷新支付配置缓存
     */
    @PostMapping("/refresh")
    public Result<Void> refreshConfig() {
        try {
            paymentConfigService.refreshConfig();
            return Result.success();
        } catch (Exception e) {
            log.error("刷新支付配置缓存失败", e);
            return Result.error(500, "刷新配置缓存失败：" + e.getMessage());
        }
    }

    /**
     * 更新支付配置
     */
    @PostMapping
    public Result<Void> updatePaymentConfig(@RequestBody Map<String, Object> request) {
        try {
            // 更新微信支付配置
            if (request.containsKey("wechat")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> wechatMap = (Map<String, Object>) request.get("wechat");
                WeChatPayConfig wechatConfig = convertToWeChatPayConfig(wechatMap);
                paymentConfigService.updateWeChatPayConfig(wechatConfig);
            }

            // 更新支付宝配置
            if (request.containsKey("alipay")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> alipayMap = (Map<String, Object>) request.get("alipay");
                AlipayConfig alipayConfig = convertToAlipayConfig(alipayMap);
                paymentConfigService.updateAlipayConfig(alipayConfig);
            }

            return Result.success();
        } catch (Exception e) {
            log.error("更新支付配置失败", e);
            return Result.error(500, "更新支付配置失败：" + e.getMessage());
        }
    }

    /**
     * 测试支付连接
     */
    @PostMapping("/test")
    public Result<String> testConnection(@RequestBody Map<String, String> request) {
        try {
            String paymentMethod = request.get("paymentMethod");
            String env = request.get("env");

            if (paymentMethod == null || paymentMethod.isEmpty()) {
                return Result.error(400, "支付方式不能为空");
            }

            // 检查支付方式是否启用
            boolean enabled = paymentConfigService.isPaymentEnabled(paymentMethod.toUpperCase());
            if (!enabled) {
                return Result.error(400, "支付方式未启用");
            }

            // TODO: 实际测试连接逻辑
            // 这里先返回成功，实际实现时需要调用支付平台API测试连接
            log.warn("支付连接测试尚未实现，当前为占位实现");
            return Result.success("连接测试成功（占位实现）");

        } catch (Exception e) {
            log.error("测试支付连接失败", e);
            return Result.error(500, "测试连接失败：" + e.getMessage());
        }
    }

    /**
     * 转换为微信支付配置
     */
    private WeChatPayConfig convertToWeChatPayConfig(Map<String, Object> map) {
        WeChatPayConfig config = new WeChatPayConfig();
        if (map.containsKey("enabled")) {
            config.setEnabled(Boolean.TRUE.equals(map.get("enabled")));
        }
        if (map.containsKey("env")) {
            config.setEnv((String) map.get("env"));
        }
        if (map.containsKey("notifyUrl")) {
            config.setNotifyUrl((String) map.get("notifyUrl"));
        }
        if (map.containsKey("sandbox")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sandboxMap = (Map<String, Object>) map.get("sandbox");
            WeChatPayConfig.WeChatPayEnvConfig sandbox = new WeChatPayConfig.WeChatPayEnvConfig();
            if (sandboxMap.containsKey("appid")) {
                sandbox.setAppid((String) sandboxMap.get("appid"));
            }
            if (sandboxMap.containsKey("mchid")) {
                sandbox.setMchid((String) sandboxMap.get("mchid"));
            }
            if (sandboxMap.containsKey("key")) {
                sandbox.setKey((String) sandboxMap.get("key"));
            }
            if (sandboxMap.containsKey("certPath")) {
                sandbox.setCertPath((String) sandboxMap.get("certPath"));
            }
            config.setSandbox(sandbox);
        }
        if (map.containsKey("production")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> productionMap = (Map<String, Object>) map.get("production");
            WeChatPayConfig.WeChatPayEnvConfig production = new WeChatPayConfig.WeChatPayEnvConfig();
            if (productionMap.containsKey("appid")) {
                production.setAppid((String) productionMap.get("appid"));
            }
            if (productionMap.containsKey("mchid")) {
                production.setMchid((String) productionMap.get("mchid"));
            }
            if (productionMap.containsKey("key")) {
                production.setKey((String) productionMap.get("key"));
            }
            if (productionMap.containsKey("certPath")) {
                production.setCertPath((String) productionMap.get("certPath"));
            }
            config.setProduction(production);
        }
        return config;
    }

    /**
     * 转换为支付宝配置
     */
    private AlipayConfig convertToAlipayConfig(Map<String, Object> map) {
        AlipayConfig config = new AlipayConfig();
        if (map.containsKey("enabled")) {
            config.setEnabled(Boolean.TRUE.equals(map.get("enabled")));
        }
        if (map.containsKey("env")) {
            config.setEnv((String) map.get("env"));
        }
        if (map.containsKey("notifyUrl")) {
            config.setNotifyUrl((String) map.get("notifyUrl"));
        }
        if (map.containsKey("sandbox")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> sandboxMap = (Map<String, Object>) map.get("sandbox");
            AlipayConfig.AlipayEnvConfig sandbox = new AlipayConfig.AlipayEnvConfig();
            if (sandboxMap.containsKey("appid")) {
                sandbox.setAppid((String) sandboxMap.get("appid"));
            }
            if (sandboxMap.containsKey("privateKey")) {
                sandbox.setPrivateKey((String) sandboxMap.get("privateKey"));
            }
            if (sandboxMap.containsKey("publicKey")) {
                sandbox.setPublicKey((String) sandboxMap.get("publicKey"));
            }
            config.setSandbox(sandbox);
        }
        if (map.containsKey("production")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> productionMap = (Map<String, Object>) map.get("production");
            AlipayConfig.AlipayEnvConfig production = new AlipayConfig.AlipayEnvConfig();
            if (productionMap.containsKey("appid")) {
                production.setAppid((String) productionMap.get("appid"));
            }
            if (productionMap.containsKey("privateKey")) {
                production.setPrivateKey((String) productionMap.get("privateKey"));
            }
            if (productionMap.containsKey("publicKey")) {
                production.setPublicKey((String) productionMap.get("publicKey"));
            }
            config.setProduction(production);
        }
        return config;
    }
}

