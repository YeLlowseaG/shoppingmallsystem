package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.service.website.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 品牌控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminBrandController")
@RequestMapping("/api/admin/website/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;

    /**
     * 分页查询品牌列表
     */
    @GetMapping("/page")
    public Result<Page<Brand>> getBrandPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer status) {
        Page<Brand> page = brandService.getBrandPage(current, size, status);
        return Result.success("获取成功", page);
    }

    /**
     * 根据ID获取品牌详情
     */
    @GetMapping("/{id}")
    public Result<Brand> getBrandById(@PathVariable Long id) {
        Brand brand = brandService.getBrandById(id);
        return Result.success("获取成功", brand);
    }

    /**
     * 创建品牌
     */
    @PostMapping
    public Result<Long> createBrand(@RequestBody Brand brand) {
        Long id = brandService.createBrand(brand);
        return Result.success("创建成功", id);
    }

    /**
     * 更新品牌
     */
    @PutMapping
    public Result<?> updateBrand(@RequestBody Brand brand) {
        brandService.updateBrand(brand);
        return Result.success("更新成功");
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return Result.success("删除成功");
    }

    /**
     * 更新品牌状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        brandService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }

    /**
     * 获取启用品牌选项列表（用于下拉框）
     */
    @GetMapping("/options")
    public Result<?> getBrandOptions() {
        try {
            System.out.println("======== 开始获取品牌选项列表 ========");
            var brands = brandService.getEnabledBrands();
            System.out.println("======== 品牌查询结果，数量: " + (brands != null ? brands.size() : 0) + " ========");
            if (brands != null) {
                brands.forEach(brand -> {
                    System.out.println("品牌: ID=" + brand.getId() + ", 名称=" + brand.getBrandName() + ", 状态=" + brand.getStatus());
                });
            }
            System.out.println("======== 返回品牌选项成功 ========");
            return Result.success("获取成功", brands);
        } catch (Exception e) {
            System.err.println("======== 获取品牌选项失败: " + e.getMessage() + " ========");
            e.printStackTrace();
            return Result.error("获取品牌选项失败: " + e.getMessage());
        }
    }
}
