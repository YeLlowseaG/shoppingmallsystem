package com.shoppingmall.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.SystemConfig;

import java.util.List;
import java.util.Map;

/**
 * 系统配置服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
public interface SystemConfigService {

    /**
     * 分页查询系统配置列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param configKey 配置键（可选）
     * @return 系统配置分页列表
     */
    Page<SystemConfig> getSystemConfigPage(Long current, Long size, String configKey);

    /**
     * 根据配置键获取配置值
     *
     * @param configKey 配置键
     * @return 配置值
     */
    String getConfigValue(String configKey);

    /**
     * 根据配置键获取配置值，带默认值
     *
     * @param configKey 配置键
     * @param defaultValue 默认值
     * @return 配置值
     */
    String getConfigValue(String configKey, String defaultValue);

    /**
     * 获取所有配置的键值对映射
     *
     * @return 配置键值对映射
     */
    Map<String, String> getAllConfigs();

    /**
     * 获取指定前缀的配置
     *
     * @param prefix 配置键前缀
     * @return 配置列表
     */
    List<SystemConfig> getConfigsByPrefix(String prefix);

    /**
     * 根据ID获取系统配置详情
     *
     * @param id 配置ID
     * @return 系统配置详情
     */
    SystemConfig getSystemConfigById(Long id);

    /**
     * 创建系统配置
     *
     * @param systemConfig 系统配置信息
     * @return 配置ID
     */
    Long createSystemConfig(SystemConfig systemConfig);

    /**
     * 更新系统配置
     *
     * @param systemConfig 系统配置信息
     */
    void updateSystemConfig(SystemConfig systemConfig);

    /**
     * 批量更新系统配置
     *
     * @param configs 配置列表
     */
    void batchUpdateConfigs(List<SystemConfig> configs);

    /**
     * 删除系统配置
     *
     * @param id 配置ID
     */
    void deleteSystemConfig(Long id);

    /**
     * 更新系统配置状态
     *
     * @param id 配置ID
     * @param status 状态
     */
    void updateStatus(Long id, Integer status);
}