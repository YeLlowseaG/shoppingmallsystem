package com.shoppingmall.controller.buyer;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器（采购者端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@RestController("buyerProductController")
@RequestMapping("/api/buyer/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品列表（只显示已上架商品）
     */
    @GetMapping("/page")
    public Result<Page<ProductVO>> getProductPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "20") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword) {
        // 买家端只查询已上架商品
        Page<ProductVO> page = productService.getProductPage(current, size, categoryId, keyword, "上架");
        return Result.success("获取成功", page);
    }

    /**
     * 根据ID获取商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductById(@PathVariable Long id) {
        ProductVO product = productService.getProductById(id);
        return Result.success("获取成功", product);
    }

    /**
     * 获取热门商品
     */
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotProducts(
            @RequestParam(defaultValue = "10") Long limit) {
        List<ProductVO> products = productService.getHotProducts(limit);
        return Result.success("获取成功", products);
    }

    /**
     * 根据分类获取推荐商品
     */
    @GetMapping("/recommend/{categoryId}")
    public Result<List<ProductVO>> getRecommendProducts(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "8") Long limit) {
        List<ProductVO> products = productService.getRecommendProducts(categoryId, limit);
        return Result.success("获取成功", products);
    }
}
