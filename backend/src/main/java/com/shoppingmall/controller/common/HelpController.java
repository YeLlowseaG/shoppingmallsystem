package com.shoppingmall.controller.common;

import com.shoppingmall.common.util.Result;
import com.shoppingmall.service.common.HelpService;
import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帮助中心控制器（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("commonHelpController")
@RequestMapping("/api/common/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpService helpService;

    /**
     * 获取帮助中心分类树
     */
    @GetMapping("/categories")
    public Result<List<HelpCategoryVO>> getCategories() {
        List<HelpCategoryVO> categories = helpService.getCategoryTree();
        return Result.success(categories);
    }

    /**
     * 根据分类ID获取文章列表
     */
    @GetMapping("/articles")
    public Result<List<HelpArticleVO>> getArticlesByCategoryId(@RequestParam Long categoryId) {
        List<HelpArticleVO> articles = helpService.getArticlesByCategoryId(categoryId);
        return Result.success(articles);
    }

    /**
     * 根据文章ID获取文章详情
     */
    @GetMapping("/article/{id}")
    public Result<HelpArticleVO> getArticleById(@PathVariable Long id) {
        HelpArticleVO article = helpService.getArticleById(id);
        return Result.success(article);
    }
}

