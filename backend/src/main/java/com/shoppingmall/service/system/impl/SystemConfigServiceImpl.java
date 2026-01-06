package com.shoppingmall.service.system.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.entity.SystemConfig;
import com.shoppingmall.repository.system.SystemConfigRepository;
import com.shoppingmall.service.system.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 系统配置服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemConfigServiceImpl implements SystemConfigService {

    private final SystemConfigRepository systemConfigRepository;

    @Override
    public Page<SystemConfig> getSystemConfigPage(Long current, Long size, String configKey, String category) {
        Page<SystemConfig> page = new Page<>(current, size);

        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();

        // 配置键筛选
        if (StringUtil.isNotBlank(configKey)) {
            wrapper.like(SystemConfig::getConfigKey, configKey)
                    .or()
                    .like(SystemConfig::getConfigName, configKey);
        }

        // 分类筛选
        if (StringUtil.isNotBlank(category)) {
            wrapper.eq(SystemConfig::getCategory, category);
        }

        // 按ID升序排序
        wrapper.orderByAsc(SystemConfig::getId);

        return systemConfigRepository.selectPage(page, wrapper);
    }

    @Override
    public String getConfigValue(String configKey) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
                .eq(SystemConfig::getStatus, 1);

        SystemConfig config = systemConfigRepository.selectOne(wrapper);
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public String getConfigValue(String configKey, String defaultValue) {
        String value = getConfigValue(configKey);
        return StringUtil.isNotBlank(value) ? value : defaultValue;
    }

    @Override
    public Map<String, String> getAllConfigs() {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getStatus, 1);

        List<SystemConfig> configs = systemConfigRepository.selectList(wrapper);
        return configs.stream()
                .collect(Collectors.toMap(
                        SystemConfig::getConfigKey,
                        config -> config.getConfigValue() != null ? config.getConfigValue() : "",
                        (existing, replacement) -> existing
                ));
    }

    @Override
    public List<SystemConfig> getConfigsByPrefix(String prefix) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(SystemConfig::getConfigKey, prefix)
                .eq(SystemConfig::getStatus, 1)
                .orderByAsc(SystemConfig::getSortOrder);

        return systemConfigRepository.selectList(wrapper);
    }

    @Override
    public SystemConfig getSystemConfigById(Long id) {
        SystemConfig config = systemConfigRepository.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "系统配置不存在");
        }
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createSystemConfig(SystemConfig systemConfig) {
        // 检查配置键是否已存在
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, systemConfig.getConfigKey());
        SystemConfig existing = systemConfigRepository.selectOne(wrapper);
        if (existing != null) {
            throw new BusinessException(400, "配置键已存在");
        }

        systemConfigRepository.insert(systemConfig);
        log.info("创建系统配置成功: {}", systemConfig.getConfigKey());
        return systemConfig.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSystemConfig(SystemConfig systemConfig) {
        SystemConfig existing = systemConfigRepository.selectById(systemConfig.getId());
        if (existing == null) {
            throw new BusinessException(404, "系统配置不存在");
        }

        // 检查配置键是否已被其他记录使用
        if (!existing.getConfigKey().equals(systemConfig.getConfigKey())) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, systemConfig.getConfigKey())
                    .ne(SystemConfig::getId, systemConfig.getId());
            SystemConfig duplicate = systemConfigRepository.selectOne(wrapper);
            if (duplicate != null) {
                throw new BusinessException(400, "配置键已存在");
            }
        }

        systemConfigRepository.updateById(systemConfig);
        log.info("更新系统配置成功: {}", systemConfig.getConfigKey());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateConfigs(List<SystemConfig> configs) {
        for (SystemConfig config : configs) {
            if (config.getId() != null) {
                SystemConfig existing = systemConfigRepository.selectById(config.getId());
                if (existing != null) {
                    existing.setConfigValue(config.getConfigValue());
                    systemConfigRepository.updateById(existing);
                }
            }
        }
        log.info("批量更新系统配置成功，共{}条", configs.size());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteSystemConfig(Long id) {
        SystemConfig existing = systemConfigRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "系统配置不存在");
        }

        systemConfigRepository.deleteById(id);
        log.info("删除系统配置成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        SystemConfig config = systemConfigRepository.selectById(id);
        if (config == null) {
            throw new BusinessException(404, "系统配置不存在");
        }

        config.setStatus(status);
        systemConfigRepository.updateById(config);
        log.info("更新系统配置状态成功: id={}, status={}", id, status);
    }
}