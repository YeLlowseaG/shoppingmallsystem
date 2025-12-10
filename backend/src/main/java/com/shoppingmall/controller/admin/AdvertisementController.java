package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Advertisement;
import com.shoppingmall.service.website.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 广告位控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminAdvertisementController")
@RequestMapping("/api/admin/website/advertisement")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    /**
     * 分页查询广告位列表
     */
    @GetMapping("/page")
    public Result<Page<Advertisement>> getAdvertisementPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String position,
            @RequestParam(required = false) Integer status) {
        Page<Advertisement> page = advertisementService.getAdvertisementPage(current, size, position, status);
        return Result.success("获取成功", page);
    }

    /**
     * 根据ID获取广告位详情
     */
    @GetMapping("/{id}")
    public Result<Advertisement> getAdvertisementById(@PathVariable Long id) {
        Advertisement advertisement = advertisementService.getAdvertisementById(id);
        return Result.success("获取成功", advertisement);
    }

    /**
     * 创建广告位
     */
    @PostMapping
    public Result<Long> createAdvertisement(@RequestBody Advertisement advertisement) {
        Long id = advertisementService.createAdvertisement(advertisement);
        return Result.success("创建成功", id);
    }

    /**
     * 更新广告位
     */
    @PutMapping
    public Result<?> updateAdvertisement(@RequestBody Advertisement advertisement) {
        advertisementService.updateAdvertisement(advertisement);
        return Result.success("更新成功");
    }

    /**
     * 删除广告位
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
        return Result.success("删除成功");
    }

    /**
     * 更新广告位状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        advertisementService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
