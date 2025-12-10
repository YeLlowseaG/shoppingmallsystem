package com.shoppingmall.service.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.shoppingmall.dto.HelpArticleDTO;
import com.shoppingmall.dto.HelpCategoryDTO;
import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;

import java.util.List;

/**
 * 帮助中心管理服务接口（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface HelpService {

    /**
     * 获取帮助中心分类树（包含所有分类，包括禁用的）
     */
    List<HelpCategoryVO> getCategoryTree();

    /**
     * 根据ID获取分类信息
     */
    HelpCategoryVO getCategoryById(Long id);

    /**
     * 新增分类
     */
    void addCategory(HelpCategoryDTO categoryDTO);

    /**
     * 更新分类
     */
    void updateCategory(Long id, HelpCategoryDTO categoryDTO);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 启用/禁用分类
     */
    void updateCategoryStatus(Long id, Integer status);

    /**
     * 获取文章列表（分页）
     */
    IPage<HelpArticleVO> getArticleList(Integer pageNum, Integer pageSize, Long categoryId, String title, Integer status);

    /**
     * 根据ID获取文章信息
     */
    HelpArticleVO getArticleById(Long id);

    /**
     * 新增文章
     */
    void addArticle(HelpArticleDTO articleDTO);

    /**
     * 更新文章
     */
    void updateArticle(Long id, HelpArticleDTO articleDTO);

    /**
     * 删除文章
     */
    void deleteArticle(Long id);

    /**
     * 启用/禁用文章
     */
    void updateArticleStatus(Long id, Integer status);
}

