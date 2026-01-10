package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.ProductCategoryDTO;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.service.product.ProductCategoryService;
import com.shoppingmall.vo.ProductCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final ProductCategoryRepository categoryRepository;

    @Override
    public List<ProductCategoryVO> getCategoryTree() {
        // 查询所有启用的分类
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder);
        List<ProductCategory> allCategories = categoryRepository.selectList(wrapper);

        // 构建树形结构
        return buildCategoryTree(allCategories, 0L);
    }

    @Override
    public List<ProductCategoryVO> getChildCategories(Long parentId) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getParentId, parentId)
                .eq(ProductCategory::getStatus, 1)
                .orderByAsc(ProductCategory::getSortOrder);
        List<ProductCategory> categories = categoryRepository.selectList(wrapper);

        return categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductCategoryVO getCategoryById(Long id) {
        ProductCategory category = categoryRepository.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(ProductCategoryDTO categoryDTO) {
        // 检查同级分类名称是否重复
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getParentId, categoryDTO.getParentId())
                .eq(ProductCategory::getCategoryName, categoryDTO.getCategoryName());
        if (categoryRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "同级分类名称已存在");
        }

        ProductCategory category = new ProductCategory();
        BeanUtils.copyProperties(categoryDTO, category);
        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        categoryRepository.insert(category);
        log.info("创建分类成功: {}", category.getCategoryName());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(ProductCategoryDTO categoryDTO) {
        ProductCategory category = categoryRepository.selectById(categoryDTO.getId());
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        // 检查同级分类名称是否重复（排除自己）
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getParentId, categoryDTO.getParentId())
                .eq(ProductCategory::getCategoryName, categoryDTO.getCategoryName())
                .ne(ProductCategory::getId, categoryDTO.getId());
        if (categoryRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "同级分类名称已存在");
        }

        BeanUtils.copyProperties(categoryDTO, category, "id");
        categoryRepository.updateById(category);
        log.info("更新分类成功: {}", category.getCategoryName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        // 检查是否有子分类
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getParentId, id);
        if (categoryRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "该分类下存在子分类，无法删除");
        }

        // TODO: 检查是否有商品使用该分类

        categoryRepository.deleteById(id);
        log.info("删除分类成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        ProductCategory category = categoryRepository.selectById(id);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        category.setStatus(status);
        categoryRepository.updateById(category);
        log.info("更新分类状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<Long> getAllCategoryIdsIncludingChildren(Long categoryId) {
        List<Long> categoryIds = new ArrayList<>();
        categoryIds.add(categoryId);
        
        // 递归获取所有子分类ID
        collectChildCategoryIds(categoryId, categoryIds);
        
        return categoryIds;
    }

    /**
     * 递归收集所有子分类ID
     */
    private void collectChildCategoryIds(Long parentId, List<Long> categoryIds) {
        LambdaQueryWrapper<ProductCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductCategory::getParentId, parentId)
                .eq(ProductCategory::getStatus, 1); // 只查询启用的分类
        List<ProductCategory> children = categoryRepository.selectList(wrapper);
        
        for (ProductCategory child : children) {
            categoryIds.add(child.getId());
            // 递归获取子分类的子分类
            collectChildCategoryIds(child.getId(), categoryIds);
        }
    }

    /**
     * 构建分类树
     */
    private List<ProductCategoryVO> buildCategoryTree(List<ProductCategory> allCategories, Long parentId) {
        List<ProductCategoryVO> result = new ArrayList<>();

        for (ProductCategory category : allCategories) {
            if (category.getParentId().equals(parentId)) {
                ProductCategoryVO vo = convertToVO(category);
                // 递归查找子分类
                vo.setChildren(buildCategoryTree(allCategories, category.getId()));
                result.add(vo);
            }
        }

        return result;
    }

    /**
     * 转换为VO
     */
    private ProductCategoryVO convertToVO(ProductCategory category) {
        ProductCategoryVO vo = new ProductCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }
}
