package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.SystemConfig;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统配置控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@RestController("adminSystemConfigController")
@RequestMapping("/api/admin/system/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    /**
     * 分页查询系统配置列表
     */
    @GetMapping("/page")
    public Result<Page<SystemConfig>> getSystemConfigPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) String configKey) {
        Page<SystemConfig> page = systemConfigService.getSystemConfigPage(current, size, configKey);
        return Result.success("获取成功", page);
    }

    /**
     * 获取所有配置的键值对映射
     */
    @GetMapping("/all")
    public Result<Map<String, String>> getAllConfigs() {
        Map<String, String> configs = systemConfigService.getAllConfigs();
        return Result.success("获取成功", configs);
    }

    /**
     * 获取指定前缀的配置
     */
    @GetMapping("/prefix/{prefix}")
    public Result<List<SystemConfig>> getConfigsByPrefix(@PathVariable String prefix) {
        List<SystemConfig> configs = systemConfigService.getConfigsByPrefix(prefix);
        return Result.success("获取成功", configs);
    }

    /**
     * 根据ID获取系统配置详情
     */
    @GetMapping("/{id}")
    public Result<SystemConfig> getSystemConfigById(@PathVariable Long id) {
        SystemConfig config = systemConfigService.getSystemConfigById(id);
        return Result.success("获取成功", config);
    }

    /**
     * 创建系统配置
     */
    @PostMapping
    public Result<Long> createSystemConfig(@RequestBody SystemConfig systemConfig) {
        Long id = systemConfigService.createSystemConfig(systemConfig);
        return Result.success("创建成功", id);
    }

    /**
     * 更新系统配置
     */
    @PutMapping
    public Result<Void> updateSystemConfig(@RequestBody SystemConfig systemConfig) {
        systemConfigService.updateSystemConfig(systemConfig);
        return Result.success("更新成功", null);
    }

    /**
     * 批量更新系统配置
     */
    @PutMapping("/batch")
    public Result<Void> batchUpdateConfigs(@RequestBody List<SystemConfig> configs) {
        systemConfigService.batchUpdateConfigs(configs);
        return Result.success("批量更新成功", null);
    }

    /**
     * 删除系统配置
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteSystemConfig(@PathVariable Long id) {
        systemConfigService.deleteSystemConfig(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新系统配置状态
     */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        systemConfigService.updateStatus(id, status);
        return Result.success("更新状态成功", null);
    }
}