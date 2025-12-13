package com.shoppingmall.service.common.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.shoppingmall.entity.Region;
import com.shoppingmall.repository.common.RegionRepository;
import com.shoppingmall.service.common.RegionService;
import com.shoppingmall.vo.RegionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 地区服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@Slf4j
@Service
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final Cache<String, Object> regionCache;

    public RegionServiceImpl(RegionRepository regionRepository, 
                            @Qualifier("regionCache") Cache<String, Object> regionCache) {
        this.regionRepository = regionRepository;
        this.regionCache = regionCache;
    }

    // 缓存键常量
    private static final String CACHE_KEY_PROVINCES = "region:provinces";
    private static final String CACHE_KEY_CHILDREN_PREFIX = "region:children:";
    private static final String CACHE_KEY_CODE_PREFIX = "region:code:";
    private static final String CACHE_KEY_ALL_REGIONS = "region:all";

    /**
     * 应用启动时预加载数据
     */
    @PostConstruct
    public void init() {
        log.info("开始预加载地区数据到缓存...");
        preloadRegions();
        log.info("地区数据预加载完成");
    }

    @Override
    public List<RegionVO> getProvinces() {
        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<RegionVO> cached = (List<RegionVO>) regionCache.getIfPresent(CACHE_KEY_PROVINCES);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Region> regions = regionRepository.selectByLevel(1);
        List<RegionVO> result = regions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 存入缓存
        regionCache.put(CACHE_KEY_PROVINCES, result);
        return result;
    }

    @Override
    public List<RegionVO> getChildrenByParentId(Long parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }

        String cacheKey = CACHE_KEY_CHILDREN_PREFIX + parentId;

        // 先从缓存获取
        @SuppressWarnings("unchecked")
        List<RegionVO> cached = (List<RegionVO>) regionCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        List<Region> regions = regionRepository.selectByParentId(parentId);
        List<RegionVO> result = regions.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 存入缓存
        regionCache.put(cacheKey, result);
        return result;
    }

    @Override
    public RegionVO getByCode(String code) {
        if (code == null || code.isEmpty()) {
            return null;
        }

        String cacheKey = CACHE_KEY_CODE_PREFIX + code;

        // 先从缓存获取
        RegionVO cached = (RegionVO) regionCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查询数据库
        Region region = regionRepository.selectByCode(code);
        if (region == null) {
            return null;
        }

        RegionVO result = convertToVO(region);

        // 存入缓存
        regionCache.put(cacheKey, result);
        return result;
    }

    @Override
    public String getFullPathByCode(String code) {
        RegionVO region = getByCode(code);
        if (region == null) {
            return "";
        }

        List<String> path = new ArrayList<>();
        RegionVO current = region;

        // 向上查找父级，构建完整路径
        while (current != null) {
            path.add(0, current.getName());
            if (current.getParentId() != null) {
                Region parent = regionRepository.selectById(current.getParentId());
                current = parent != null ? convertToVO(parent) : null;
            } else {
                current = null;
            }
        }

        return String.join("-", path);
    }

    /**
     * 预加载所有地区数据到缓存
     * 应用启动时执行，减少首次查询延迟
     */
    @Override
    public void preloadRegions() {
        try {
            // 1. 预加载所有省份
            List<RegionVO> provinces = getProvinces();
            log.info("预加载省份数据: {} 条", provinces.size());

            // 2. 预加载所有地区数据（用于快速查找）
            List<Region> allRegions = regionRepository.selectAllEnabled();
            
            // 3. 批量缓存所有地区编码
            for (Region region : allRegions) {
                RegionVO vo = convertToVO(region);
                regionCache.put(CACHE_KEY_CODE_PREFIX + region.getCode(), vo);
            }

            // 4. 批量缓存所有子级关系
            Map<Long, List<RegionVO>> childrenMap = allRegions.stream()
                    .filter(r -> r.getParentId() != null)
                    .collect(Collectors.groupingBy(
                            Region::getParentId,
                            Collectors.mapping(this::convertToVO, Collectors.toList())
                    ));

            childrenMap.forEach((parentId, children) -> {
                // 按 sort_order 排序
                children.sort(Comparator.comparing(RegionVO::getSortOrder)
                        .thenComparing(RegionVO::getId));
                regionCache.put(CACHE_KEY_CHILDREN_PREFIX + parentId, children);
            });

            log.info("预加载地区数据完成: 总计 {} 条", allRegions.size());

        } catch (Exception e) {
            log.error("预加载地区数据失败", e);
        }
    }

    @Override
    public void clearCache() {
        regionCache.invalidateAll();
        log.info("地区缓存已清除");
    }

    /**
     * 实体转VO
     */
    private RegionVO convertToVO(Region region) {
        RegionVO vo = new RegionVO();
        BeanUtils.copyProperties(region, vo);
        return vo;
    }
}

