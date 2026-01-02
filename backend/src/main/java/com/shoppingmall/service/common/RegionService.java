package com.shoppingmall.service.common;

import com.shoppingmall.vo.RegionVO;
import java.util.List;

/**
 * 地区服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
public interface RegionService {
    /**
     * 获取所有省份（一级）
     */
    List<RegionVO> getProvinces();

    /**
     * 根据父级ID获取子级地区
     */
    List<RegionVO> getChildrenByParentId(Long parentId);

    /**
     * 根据编码获取地区信息
     */
    RegionVO getByCode(String code);

    /**
     * 根据编码获取完整路径（省-市-区）
     */
    String getFullPathByCode(String code);

    /**
     * 预加载所有地区数据到缓存
     */
    void preloadRegions();

    /**
     * 清除缓存
     */
    void clearCache();
}





























































