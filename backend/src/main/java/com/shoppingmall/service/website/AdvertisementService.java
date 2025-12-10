package com.shoppingmall.service.website;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.Advertisement;

import java.util.List;

/**
 * 广告位服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface AdvertisementService {

    /**
     * 分页查询广告位列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param position 广告位置（可选）
     * @param status 状态（可选）
     * @return 广告位分页列表
     */
    Page<Advertisement> getAdvertisementPage(Long current, Long size, String position, Integer status);

    /**
     * 根据ID获取广告位详情
     *
     * @param id 广告位ID
     * @return 广告位详情
     */
    Advertisement getAdvertisementById(Long id);

    /**
     * 创建广告位
     *
     * @param advertisement 广告位信息
     * @return 广告位ID
     */
    Long createAdvertisement(Advertisement advertisement);

    /**
     * 更新广告位
     *
     * @param advertisement 广告位信息
     */
    void updateAdvertisement(Advertisement advertisement);

    /**
     * 删除广告位
     *
     * @param id 广告位ID
     */
    void deleteAdvertisement(Long id);

    /**
     * 更新广告位状态
     *
     * @param id 广告位ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 根据位置获取启用的广告列表（买家端使用）
     *
     * @param position 广告位置
     * @return 启用的广告列表
     */
    List<Advertisement> getActiveAdvertisementsByPosition(String position);

    /**
     * 获取所有楼层广告（买家端使用）
     *
     * @return 楼层广告列表（按楼层编号排序）
     */
    List<Advertisement> getAllFloorAdvertisements();
}
