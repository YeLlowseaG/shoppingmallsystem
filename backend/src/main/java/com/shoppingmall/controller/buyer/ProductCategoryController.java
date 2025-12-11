package com.shoppingmall.controller.buyer;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.product.ProductCategoryService;
import com.shoppingmall.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品分类控制器（采购者端）
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@RestController("buyerProductCategoryController")
@RequestMapping("/api/buyer/product-category")
@RequiredArgsConstructor
public class ProductCategoryController {

    private final ProductCategoryService categoryService;

    /**
     * 获取分类树（用于导航菜单）
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
}
