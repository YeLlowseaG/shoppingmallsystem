package com.shoppingmall.service.common;

import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;

import java.util.List;

/**
 * 帮助中心服务接口（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
public interface HelpService {

    /**
     * 获取帮助中心分类树（只返回启用的分类）
     */
    List<HelpCategoryVO> getCategoryTree();

    /**
     * 根据分类ID获取文章列表（只返回启用的文章）
     */
    List<HelpArticleVO> getArticlesByCategoryId(Long categoryId);

    /**
     * 根据文章ID获取文章详情
     */
    HelpArticleVO getArticleById(Long id);
}




