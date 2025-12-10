package com.shoppingmall.service.website;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.Banner;

import java.util.List;

/**
 * 轮播图服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface BannerService {

    /**
     * 分页查询轮播图列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param status 状态（可选）
     * @return 轮播图分页列表
     */
    Page<Banner> getBannerPage(Long current, Long size, Integer status);

    /**
     * 根据ID获取轮播图详情
     *
     * @param id 轮播图ID
     * @return 轮播图详情
     */
    Banner getBannerById(Long id);

    /**
     * 创建轮播图
     *
     * @param banner 轮播图信息
     * @return 轮播图ID
     */
    Long createBanner(Banner banner);

    /**
     * 更新轮播图
     *
     * @param banner 轮播图信息
     */
    void updateBanner(Banner banner);

    /**
     * 删除轮播图
     *
     * @param id 轮播图ID
     */
    void deleteBanner(Long id);

    /**
     * 更新轮播图状态
     *
     * @param id 轮播图ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取启用的轮播图列表（买家端使用）
     *
     * @return 启用的轮播图列表
     */
    List<Banner> getActiveBanners();
}
