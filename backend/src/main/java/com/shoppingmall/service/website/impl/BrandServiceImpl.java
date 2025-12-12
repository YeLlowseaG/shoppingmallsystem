package com.shoppingmall.service.website.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.repository.website.BrandRepository;
import com.shoppingmall.service.website.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 品牌服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public Page<Brand> getBrandPage(Long current, Long size, Integer status) {
        Page<Brand> page = new Page<>(current, size);

        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (status != null) {
            wrapper.eq(Brand::getStatus, status);
        }

        // 按排序号升序
        wrapper.orderByAsc(Brand::getSortOrder);
        wrapper.orderByDesc(Brand::getCreateTime);

        return brandRepository.selectPage(page, wrapper);
    }

    @Override
    public Brand getBrandById(Long id) {
        Brand brand = brandRepository.selectById(id);
        if (brand == null) {
            throw new BusinessException(404, "品牌不存在");
        }
        return brand;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBrand(Brand brand) {
        brandRepository.insert(brand);
        return brand.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBrand(Brand brand) {
        Brand existing = brandRepository.selectById(brand.getId());
        if (existing == null) {
            throw new BusinessException(404, "品牌不存在");
        }
        brandRepository.updateById(brand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        Brand existing = brandRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "品牌不存在");
        }
        brandRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Brand brand = brandRepository.selectById(id);
        if (brand == null) {
            throw new BusinessException(404, "品牌不存在");
        }
        brand.setStatus(status);
        brandRepository.updateById(brand);
    }

    @Override
    public List<Brand> getActiveBrands(Integer limit) {
        LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Brand::getStatus, 1); // 只查询启用状态

        // 按排序号升序
        wrapper.orderByAsc(Brand::getSortOrder);

        // 限制数量
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        return brandRepository.selectList(wrapper);
    }

    @Override
    public List<Brand> getEnabledBrands() {
        System.out.println("======== BrandService: 开始查询启用品牌 ========");
        try {
            LambdaQueryWrapper<Brand> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Brand::getStatus, 1); // 只查询启用状态
            wrapper.orderByAsc(Brand::getSortOrder, Brand::getId);
            System.out.println("======== BrandService: 查询条件构建完成 ========");
            
            List<Brand> brands = brandRepository.selectList(wrapper);
            System.out.println("======== BrandService: 数据库查询完成，结果数量: " + (brands != null ? brands.size() : 0) + " ========");
            
            return brands;
        } catch (Exception e) {
            System.err.println("======== BrandService: 查询启用品牌失败: " + e.getMessage() + " ========");
            e.printStackTrace();
            throw e;
        }
    }
}
