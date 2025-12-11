package com.shoppingmall.controller.admin;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.ProductCategoryDTO;
import com.shoppingmall.service.product.ProductCategoryService;
import com.shoppingmall.vo.ProductCategoryVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器（管理端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@RestController("adminProductCategoryController")
@RequestMapping("/api/admin/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 获取分类树
     */
    @GetMapping("/tree")
    public Result<List<ProductCategoryVO>> getCategoryTree() {
        List<ProductCategoryVO> tree = categoryService.getCategoryTree();
        return Result.success("获取成功", tree);
    }

    /**
     * 根据父分类ID获取子分类
     */
    @GetMapping("/children/{parentId}")
    public Result<List<ProductCategoryVO>> getChildCategories(@PathVariable Long parentId) {
        List<ProductCategoryVO> children = categoryService.getChildCategories(parentId);
        return Result.success("获取成功", children);
    }

    /**
     * 根据ID获取分类详情
     */
    @GetMapping("/{id}")
    public Result<ProductCategoryVO> getCategoryById(@PathVariable Long id) {
        ProductCategoryVO category = categoryService.getCategoryById(id);
        return Result.success("获取成功", category);
    }

    /**
     * 创建分类
     */
    @PostMapping
    public Result<Long> createCategory(@Valid @RequestBody ProductCategoryDTO categoryDTO) {
        Long id = categoryService.createCategory(categoryDTO);
        return Result.success("创建成功", id);
    }

    /**
     * 更新分类
     */
    @PutMapping
    public Result<?> updateCategory(@Valid @RequestBody ProductCategoryDTO categoryDTO) {
        categoryService.updateCategory(categoryDTO);
        return Result.success("更新成功");
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Result<?> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success("删除成功");
    }

    /**
     * 更新分类状态
     */
    @PutMapping("/{id}/status")
    public Result<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success("状态更新成功");
    }
}
