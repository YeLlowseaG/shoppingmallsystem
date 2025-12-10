package com.shoppingmall.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.common.util.Result;
import com.shoppingmall.dto.HelpArticleDTO;
import com.shoppingmall.dto.HelpCategoryDTO;
import com.shoppingmall.service.admin.HelpService;
import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 帮助中心管理控制器（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@RestController("adminHelpController")
@RequestMapping("/api/admin/help")
@RequiredArgsConstructor
public class HelpController {

    private final HelpService helpService;

    // ==================== 分类管理 ====================

    /**
     * 获取帮助中心分类树
     */
    @GetMapping("/categories/tree")
    public Result<List<HelpCategoryVO>> getCategoryTree() {
        List<HelpCategoryVO> categories = helpService.getCategoryTree();
        return Result.success(categories);
    }

    /**
     * 根据ID获取分类信息
     */
    @GetMapping("/category/{id}")
    public Result<HelpCategoryVO> getCategoryById(@PathVariable Long id) {
        HelpCategoryVO category = helpService.getCategoryById(id);
        return Result.success(category);
    }

    /**
     * 新增分类
     */
    @PostMapping("/category")
    public Result<Void> addCategory(@RequestBody @Validated HelpCategoryDTO categoryDTO) {
        helpService.addCategory(categoryDTO);
        return Result.success();
    }

    /**
     * 更新分类
     */
    @PutMapping("/category/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @RequestBody @Validated HelpCategoryDTO categoryDTO) {
        helpService.updateCategory(id, categoryDTO);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        helpService.deleteCategory(id);
        return Result.success();
    }

    /**
     * 启用/禁用分类
     */
    @PutMapping("/category/{id}/status")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @RequestParam Integer status) {
        helpService.updateCategoryStatus(id, status);
        return Result.success();
    }

    // ==================== 文章管理 ====================

    /**
     * 获取文章列表（分页）
     */
    @GetMapping("/articles")
    public Result<IPage<HelpArticleVO>> getArticleList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Integer status
    ) {
        IPage<HelpArticleVO> articlePage = helpService.getArticleList(pageNum, pageSize, categoryId, title, status);
        return Result.success(articlePage);
    }

    /**
     * 根据ID获取文章信息
     */
    @GetMapping("/article/{id}")
    public Result<HelpArticleVO> getArticleById(@PathVariable Long id) {
        HelpArticleVO article = helpService.getArticleById(id);
        return Result.success(article);
    }

    /**
     * 新增文章
     */
    @PostMapping("/article")
    public Result<Void> addArticle(@RequestBody @Validated HelpArticleDTO articleDTO) {
        helpService.addArticle(articleDTO);
        return Result.success();
    }

    /**
     * 更新文章
     */
    @PutMapping("/article/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, @RequestBody @Validated HelpArticleDTO articleDTO) {
        helpService.updateArticle(id, articleDTO);
        return Result.success();
    }

    /**
     * 删除文章
     */
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        helpService.deleteArticle(id);
        return Result.success();
    }

    /**
     * 启用/禁用文章
     */
    @PutMapping("/article/{id}/status")
    public Result<Void> updateArticleStatus(@PathVariable Long id, @RequestParam Integer status) {
        helpService.updateArticleStatus(id, status);
        return Result.success();
    }
}

