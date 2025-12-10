package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Banner;
import com.shoppingmall.service.website.BannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 轮播图控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminBannerController")
@RequestMapping("/api/admin/website/banner")
@RequiredArgsConstructor
public class BannerController {

    private final BannerService bannerService;

    /**
     * 分页查询轮播图列表
     */
    @GetMapping("/page")
    public Result<Page<Banner>> getBannerPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        Page<Banner> page = bannerService.getBannerPage(current, size, status);
        return Result.success("获取成功", page);
    }

    /**
     * 根据ID获取轮播图详情
     */
    @GetMapping("/{id}")
    public Result<Banner> getBannerById(@PathVariable Long id) {
        Banner banner = bannerService.getBannerById(id);
        return Result.success("获取成功", banner);
    }

    /**
     * 创建轮播图
     */
    @PostMapping
    public Result<Long> createBanner(@RequestBody Banner banner) {
        Long id = bannerService.createBanner(banner);
        return Result.success("创建成功", id);
    }

    /**
     * 更新轮播图
     */
    @PutMapping
    public Result<?> updateBanner(@RequestBody Banner banner) {
        bannerService.updateBanner(banner);
        return Result.success("更新成功");
    }

    /**
     * 删除轮播图
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return Result.success("删除成功");
    }

    /**
     * 更新轮播图状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        bannerService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
