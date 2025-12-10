package com.shoppingmall.service.website.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.entity.Banner;
import com.shoppingmall.repository.website.BannerRepository;
import com.shoppingmall.service.website.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 轮播图服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;

    @Override
    public Page<Banner> getBannerPage(Long current, Long size, Integer status) {
        Page<Banner> page = new Page<>(current, size);

        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (status != null) {
            wrapper.eq(Banner::getStatus, status);
        }

        // 按排序号升序
        wrapper.orderByAsc(Banner::getSortOrder);
        wrapper.orderByDesc(Banner::getCreateTime);

        return bannerRepository.selectPage(page, wrapper);
    }

    @Override
    public Banner getBannerById(Long id) {
        Banner banner = bannerRepository.selectById(id);
        if (banner == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        return banner;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createBanner(Banner banner) {
        bannerRepository.insert(banner);
        return banner.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBanner(Banner banner) {
        Banner existing = bannerRepository.selectById(banner.getId());
        if (existing == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        bannerRepository.updateById(banner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBanner(Long id) {
        Banner existing = bannerRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        bannerRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Banner banner = bannerRepository.selectById(id);
        if (banner == null) {
            throw new BusinessException(404, "轮播图不存在");
        }
        banner.setStatus(status);
        bannerRepository.updateById(banner);
    }

    @Override
    public List<Banner> getActiveBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1); // 只查询启用状态

        // 时间过滤（可选）
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(Banner::getStartTime).or().le(Banner::getStartTime, now));
        wrapper.and(w -> w.isNull(Banner::getEndTime).or().ge(Banner::getEndTime, now));

        // 按排序号升序
        wrapper.orderByAsc(Banner::getSortOrder);

        return bannerRepository.selectList(wrapper);
    }
}
