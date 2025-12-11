package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Advertisement;
import com.shoppingmall.entity.Banner;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.service.website.AdvertisementService;
import com.shoppingmall.service.website.BannerService;
import com.shoppingmall.service.website.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 网站内容控制器（买家端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("buyerWebsiteController")
@RequestMapping("/api/buyer/website")
@RequiredArgsConstructor
public class WebsiteController {

    private final BannerService bannerService;
    private final BrandService brandService;
    private final AdvertisementService advertisementService;

    /**
     * 获取启用的轮播图列表
     */
    @GetMapping("/banners")
    public Result<List<Banner>> getActiveBanners() {
        List<Banner> banners = bannerService.getActiveBanners();
        return Result.success("获取成功", banners);
    }

    /**
     * 获取启用的品牌列表
     */
    @GetMapping("/brands")
    public Result<List<Brand>> getActiveBrands(@RequestParam(required = false) Integer limit) {
        List<Brand> brands = brandService.getActiveBrands(limit);
        return Result.success("获取成功", brands);
    }

    /**
     * 根据位置获取广告列表
     */
    @GetMapping("/advertisements/{position}")
    public Result<List<Advertisement>> getAdvertisementsByPosition(@PathVariable String position) {
        List<Advertisement> advertisements = advertisementService.getActiveAdvertisementsByPosition(position);
        return Result.success("获取成功", advertisements);
    }

    /**
     * 获取所有楼层广告
     */
    @GetMapping("/advertisements/floors")
    public Result<List<Advertisement>> getAllFloorAdvertisements() {
        List<Advertisement> advertisements = advertisementService.getAllFloorAdvertisements();
        return Result.success("获取成功", advertisements);
    }
}
