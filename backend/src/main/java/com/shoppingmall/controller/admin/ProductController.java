package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 商品控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@RestController("adminProductController")
@RequestMapping("/api/admin/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 分页查询商品列表
     */
    @GetMapping("/page")
    public Result<Page<ProductVO>> getProductPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String status) {
        Page<ProductVO> page = productService.getProductPage(current, size, categoryId, keyword, brand, status);
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
     * 创建商品
     */
    @PostMapping
    public Result<Long> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        Long id = productService.createProduct(productDTO);
        return Result.success("创建成功", id);
    }

    /**
     * 更新商品
     */
    @PutMapping
    public Result<?> updateProduct(@Valid @RequestBody ProductDTO productDTO) {
        productService.updateProduct(productDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success("删除成功");
    }

    /**
     * 更新商品状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        productService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
