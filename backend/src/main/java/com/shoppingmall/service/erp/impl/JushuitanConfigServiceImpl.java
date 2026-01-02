package com.shoppingmall.service.erp.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.shoppingmall.common.util.JushuitanHttpUtil;
import com.shoppingmall.dto.JushuitanConfigDTO;
import com.shoppingmall.entity.JushuitanConfig;
import com.shoppingmall.mapper.JushuitanConfigMapper;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.vo.JushuitanConfigVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

/**
 * 聚水潭配置Service实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
@Slf4j
@Service
public class JushuitanConfigServiceImpl implements JushuitanConfigService {

    @Resource
    private JushuitanConfigMapper jushuitanConfigMapper;

    @Override
    public JushuitanConfigVO getConfig() {
        JushuitanConfig config = jushuitanConfigMapper.selectOne(new QueryWrapper<JushuitanConfig>().last("LIMIT 1"));
        if (config == null) {
            return null;
        }
        return convertToVO(config);
    }

    @Override
    public JushuitanConfigVO saveOrUpdateConfig(JushuitanConfigDTO dto) {
        JushuitanConfig config = jushuitanConfigMapper.selectOne(new QueryWrapper<JushuitanConfig>().last("LIMIT 1"));

        if (config == null) {
            // 新增
            config = new JushuitanConfig();
            BeanUtils.copyProperties(dto, config);
            jushuitanConfigMapper.insert(config);
        } else {
            // 更新 - 只更新非null字段
            if (dto.getApiUrl() != null) {
                config.setApiUrl(dto.getApiUrl());
            }
            if (dto.getAppKey() != null) {
                config.setAppKey(dto.getAppKey());
            }
            // AppSecret: 只有当DTO中有值时才更新（防止前端传null覆盖真实值）
            if (dto.getAppSecret() != null && !dto.getAppSecret().isEmpty()) {
                config.setAppSecret(dto.getAppSecret());
            }
            // AccessToken: 只有当DTO中有值时才更新
            if (dto.getAccessToken() != null && !dto.getAccessToken().isEmpty()) {
                config.setAccessToken(dto.getAccessToken());
            }
            // 环境类型
            if (dto.getEnvType() != null) {
                config.setEnvType(dto.getEnvType());
            }
            // 测试环境配置
            if (dto.getTestApiUrl() != null) {
                config.setTestApiUrl(dto.getTestApiUrl());
            }
            if (dto.getTestAppKey() != null) {
                config.setTestAppKey(dto.getTestAppKey());
            }
            if (dto.getTestAppSecret() != null && !dto.getTestAppSecret().isEmpty()) {
                config.setTestAppSecret(dto.getTestAppSecret());
            }
            // 测试环境AccessToken: 只有当DTO中有值时才更新
            if (dto.getTestAccessToken() != null && !dto.getTestAccessToken().isEmpty()) {
                config.setTestAccessToken(dto.getTestAccessToken());
            }
            // 测试环境店铺ID
            if (dto.getTestShopId() != null) {
                config.setTestShopId(dto.getTestShopId());
            }
            // 生产环境店铺ID
            if (dto.getShopId() != null) {
                config.setShopId(dto.getShopId());
            }
            if (dto.getPartnerId() != null) {
                config.setPartnerId(dto.getPartnerId());
            }
            if (dto.getEnabled() != null) {
                config.setEnabled(dto.getEnabled());
            }
            if (dto.getAutoPushOrder() != null) {
                config.setAutoPushOrder(dto.getAutoPushOrder());
            }
            if (dto.getAutoPullLogistics() != null) {
                config.setAutoPullLogistics(dto.getAutoPullLogistics());
            }
            if (dto.getPullInterval() != null) {
                config.setPullInterval(dto.getPullInterval());
            }
            if (dto.getRemark() != null) {
                config.setRemark(dto.getRemark());
            }
            jushuitanConfigMapper.updateById(config);
        }

        return convertToVO(config);
    }

    @Override
    public String testConnection() {
        JushuitanConfigVO config = getEnabledConfig();
        if (config == null) {
            return "未配置聚水潭或配置未启用";
        }

        try {
            // 调用聚水潭测试接口（后续根据实际API文档调整）
            String response = JushuitanHttpUtil.post(
                config.getApiUrl(),
                config.getAppKey(),
                config.getAppSecret(),
                config.getAccessToken(),
                "test.connection",
                null
            );

            log.info("聚水潭连接测试成功: {}", response);
            return "连接成功";
        } catch (Exception e) {
            log.error("聚水潭连接测试失败", e);
            return "连接失败: " + e.getMessage();
        }
    }

    @Override
    public JushuitanConfigVO getEnabledConfig() {
        JushuitanConfig config = jushuitanConfigMapper.selectOne(
            new QueryWrapper<JushuitanConfig>()
                .eq("enabled", 1)
                .last("LIMIT 1")
        );
        return config == null ? null : getActiveEnvConfig(config);
    }

    /**
     * 转换为VO
     */
    private JushuitanConfigVO convertToVO(JushuitanConfig config) {
        JushuitanConfigVO vo = new JushuitanConfigVO();
        BeanUtils.copyProperties(config, vo);

        // 生产环境 AppSecret 脱敏
        if (vo.getAppSecret() != null && vo.getAppSecret().length() > 8) {
            vo.setAppSecret(vo.getAppSecret().substring(0, 8) + "****");
        }

        // 生产环境 AccessToken 脱敏
        if (vo.getAccessToken() != null && vo.getAccessToken().length() > 8) {
            vo.setAccessToken(vo.getAccessToken().substring(0, 8) + "****");
        }

        // 测试环境 AppSecret 脱敏
        if (vo.getTestAppSecret() != null && vo.getTestAppSecret().length() > 8) {
            vo.setTestAppSecret(vo.getTestAppSecret().substring(0, 8) + "****");
        }

        // 测试环境 AccessToken 脱敏
        if (vo.getTestAccessToken() != null && vo.getTestAccessToken().length() > 8) {
            vo.setTestAccessToken(vo.getTestAccessToken().substring(0, 8) + "****");
        }

        return vo;
    }

    /**
     * 获取当前环境的配置（根据envType返回对应环境的配置）
     */
    private JushuitanConfigVO getActiveEnvConfig(JushuitanConfig config) {
        JushuitanConfigVO vo = new JushuitanConfigVO();
        BeanUtils.copyProperties(config, vo);

        // 根据环境类型，将对应环境的配置映射到主配置字段
        if ("test".equals(config.getEnvType())) {
            // 测试环境：使用test_*字段
            vo.setApiUrl(config.getTestApiUrl());
            vo.setAppKey(config.getTestAppKey());
            vo.setAppSecret(config.getTestAppSecret());
            vo.setAccessToken(config.getTestAccessToken());
            vo.setShopId(config.getTestShopId());
        } else {
            // 生产环境：使用原有字段（默认）
            // 已经通过BeanUtils.copyProperties复制了
        }

        return vo;
    }
}
