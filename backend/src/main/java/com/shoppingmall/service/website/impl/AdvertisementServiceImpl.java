package com.shoppingmall.service.website.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.entity.Advertisement;
import com.shoppingmall.repository.website.AdvertisementRepository;
import com.shoppingmall.service.website.AdvertisementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 广告位服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementServiceImpl implements AdvertisementService {

    private final AdvertisementRepository advertisementRepository;

    @Override
    public Page<Advertisement> getAdvertisementPage(Long current, Long size, String position, Integer status) {
        Page<Advertisement> page = new Page<>(current, size);

        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();

        // 位置筛选
        if (StringUtil.isNotBlank(position)) {
            wrapper.eq(Advertisement::getAdPosition, position);
        }

        // 状态筛选
        if (status != null) {
            wrapper.eq(Advertisement::getStatus, status);
        }

        // 按排序号升序
        wrapper.orderByAsc(Advertisement::getSortOrder);
        wrapper.orderByDesc(Advertisement::getCreateTime);

        return advertisementRepository.selectPage(page, wrapper);
    }

    @Override
    public Advertisement getAdvertisementById(Long id) {
        Advertisement advertisement = advertisementRepository.selectById(id);
        if (advertisement == null) {
            throw new BusinessException(404, "广告位不存在");
        }
        return advertisement;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAdvertisement(Advertisement advertisement) {
        advertisementRepository.insert(advertisement);
        return advertisement.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAdvertisement(Advertisement advertisement) {
        Advertisement existing = advertisementRepository.selectById(advertisement.getId());
        if (existing == null) {
            throw new BusinessException(404, "广告位不存在");
        }
        advertisementRepository.updateById(advertisement);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAdvertisement(Long id) {
        Advertisement existing = advertisementRepository.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "广告位不存在");
        }
        advertisementRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        Advertisement advertisement = advertisementRepository.selectById(id);
        if (advertisement == null) {
            throw new BusinessException(404, "广告位不存在");
        }
        advertisement.setStatus(status);
        advertisementRepository.updateById(advertisement);
    }

    @Override
    public List<Advertisement> getActiveAdvertisementsByPosition(String position) {
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisement::getStatus, 1); // 只查询启用状态
        wrapper.eq(Advertisement::getAdPosition, position);

        // 时间过滤（可选）
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(Advertisement::getStartTime).or().le(Advertisement::getStartTime, now));
        wrapper.and(w -> w.isNull(Advertisement::getEndTime).or().ge(Advertisement::getEndTime, now));

        // 按排序号升序
        wrapper.orderByAsc(Advertisement::getSortOrder);

        return advertisementRepository.selectList(wrapper);
    }

    @Override
    public List<Advertisement> getAllFloorAdvertisements() {
        LambdaQueryWrapper<Advertisement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Advertisement::getStatus, 1); // 只查询启用状态
        wrapper.likeRight(Advertisement::getAdPosition, "floor_"); // 位置以 floor_ 开头

        // 时间过滤（可选）
        LocalDateTime now = LocalDateTime.now();
        wrapper.and(w -> w.isNull(Advertisement::getStartTime).or().le(Advertisement::getStartTime, now));
        wrapper.and(w -> w.isNull(Advertisement::getEndTime).or().ge(Advertisement::getEndTime, now));

        // 按位置排序（floor_1, floor_2, ...）
        wrapper.orderByAsc(Advertisement::getAdPosition);
        wrapper.orderByAsc(Advertisement::getSortOrder);

        return advertisementRepository.selectList(wrapper);
    }
}
