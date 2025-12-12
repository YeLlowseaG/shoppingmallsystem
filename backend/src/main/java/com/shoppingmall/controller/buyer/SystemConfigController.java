package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 系统配置控制器（买家端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@RestController("buyerSystemConfigController")
@RequestMapping("/api/buyer/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 获取所有公开配置的键值对映射
     */
    @GetMapping("/public")
    public Result<Map<String, String>> getPublicConfigs() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        return Result.success("获取成功", configs);
    }

    /**
     * 根据配置键获取配置值
     */
    @GetMapping("/{configKey}")
    public Result<String> getConfigValue(@PathVariable String configKey) {
        String value = systemConfigService.getConfigValue(configKey);
        return Result.success("获取成功", value);
    }
}