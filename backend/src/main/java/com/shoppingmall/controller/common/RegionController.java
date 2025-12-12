package com.shoppingmall.controller.common;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.common.RegionService;
import com.shoppingmall.vo.RegionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 地区控制器
 *
 * @author ShoppingMall Team
 * @date 2025-12-12
 */
@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    /**
     * 获取所有省份（一级）
     */
    @GetMapping("/provinces")
    public Result<List<RegionVO>> getProvinces() {
        List<RegionVO> provinces = regionService.getProvinces();
        return Result.success(provinces);
    }

    /**
     * 根据父级ID获取子级地区
     */
    @GetMapping("/children/{parentId}")
    public Result<List<RegionVO>> getChildren(@PathVariable Long parentId) {
        List<RegionVO> children = regionService.getChildrenByParentId(parentId);
        return Result.success(children);
    }

    /**
     * 根据编码获取地区信息
     */
    @GetMapping("/code/{code}")
    public Result<RegionVO> getByCode(@PathVariable String code) {
        RegionVO region = regionService.getByCode(code);
        if (region == null) {
            return Result.error(404, "地区不存在");
        }
        return Result.success(region);
    }

    /**
     * 根据编码获取完整路径
     */
    @GetMapping("/path/{code}")
    public Result<String> getFullPath(@PathVariable String code) {
        String path = regionService.getFullPathByCode(code);
        return Result.success(path);
    }
}

