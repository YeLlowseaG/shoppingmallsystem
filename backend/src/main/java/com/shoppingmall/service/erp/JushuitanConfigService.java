package com.shoppingmall.service.erp;

import com.shoppingmall.dto.JushuitanConfigDTO;
import com.shoppingmall.vo.JushuitanConfigVO;

/**
 * 聚水潭配置Service接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-24
 */
public interface JushuitanConfigService {

    /**
     * 获取聚水潭配置
     *
     * @return 配置信息
     */
    JushuitanConfigVO getConfig();

    /**
     * 保存或更新聚水潭配置
     *
     * @param dto 配置DTO
     * @return 保存后的配置信息
     */
    JushuitanConfigVO saveOrUpdateConfig(JushuitanConfigDTO dto);

    /**
     * 测试聚水潭连接
     *
     * @return 测试结果描述
     */
    String testConnection();

    /**
     * 获取启用的配置
     *
     * @return 启用的配置，如果没有启用则返回null
     */
    JushuitanConfigVO getEnabledConfig();
}
