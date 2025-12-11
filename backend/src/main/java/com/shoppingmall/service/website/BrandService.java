package com.shoppingmall.service.website;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.entity.Brand;

import java.util.List;

/**
 * 品牌服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface BrandService {

    /**
     * 分页查询品牌列表
     *
     * @param current 当前页
     * @param size 每页大小
     * @param status 状态（可选）
     * @return 品牌分页列表
     */
    Page<Brand> getBrandPage(Long current, Long size, Integer status);

    /**
     * 根据ID获取品牌详情
     *
     * @param id 品牌ID
     * @return 品牌详情
     */
    Brand getBrandById(Long id);

    /**
     * 创建品牌
     *
     * @param brand 品牌信息
     * @return 品牌ID
     */
    Long createBrand(Brand brand);

    /**
     * 更新品牌
     *
     * @param brand 品牌信息
     */
    void updateBrand(Brand brand);

    /**
     * 删除品牌
     *
     * @param id 品牌ID
     */
    void deleteBrand(Long id);

    /**
     * 更新品牌状态
     *
     * @param id 品牌ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取启用的品牌列表（买家端使用）
     *
     * @param limit 数量限制（可选）
     * @return 启用的品牌列表
     */
    List<Brand> getActiveBrands(Integer limit);
}
