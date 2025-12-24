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
            // 更新
            BeanUtils.copyProperties(dto, config);
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
        return config == null ? null : convertToVO(config);
    }

    /**
     * 转换为VO
     */
    private JushuitanConfigVO convertToVO(JushuitanConfig config) {
        JushuitanConfigVO vo = new JushuitanConfigVO();
        BeanUtils.copyProperties(config, vo);
        // AppSecret脱敏
        if (vo.getAppSecret() != null && vo.getAppSecret().length() > 8) {
            vo.setAppSecret(vo.getAppSecret().substring(0, 8) + "****");
        }
        return vo;
    }
}
