package com.shoppingmall.service.product;

import com.shoppingmall.dto.ProductCategoryDTO;
import com.shoppingmall.vo.ProductCategoryVO;

import java.util.List;

/**
 * 商品分类服务接口
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
public interface ProductCategoryService {

    /**
     * 获取所有分类树（包含子分类）
     *
     * @return 分类树列表
     */
    List<ProductCategoryVO> getCategoryTree();

    /**
     * 根据父分类ID获取子分类列表
     *
     * @param parentId 父分类ID
     * @return 子分类列表
     */
    List<ProductCategoryVO> getChildCategories(Long parentId);

    /**
     * 根据ID获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    ProductCategoryVO getCategoryById(Long id);

    /**
     * 创建分类
     *
     * @param categoryDTO 分类信息
     * @return 分类ID
     */
    Long createCategory(ProductCategoryDTO categoryDTO);

    /**
     * 更新分类
     *
     * @param categoryDTO 分类信息
     */
    void updateCategory(ProductCategoryDTO categoryDTO);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 更新分类状态
     *
     * @param id 分类ID
     * @param status 状态（0-禁用，1-启用）
     */
    void updateStatus(Long id, Integer status);

    /**
     * 获取指定分类及其所有子分类的ID列表（递归）
     *
     * @param categoryId 分类ID
     * @return 分类ID列表（包含自身及所有子分类）
     */
    List<Long> getAllCategoryIdsIncludingChildren(Long categoryId);
}
