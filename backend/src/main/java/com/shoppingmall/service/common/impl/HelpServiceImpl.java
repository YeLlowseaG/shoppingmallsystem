package com.shoppingmall.service.common.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.entity.HelpArticle;
import com.shoppingmall.entity.HelpCategory;
import com.shoppingmall.repository.help.HelpArticleRepository;
import com.shoppingmall.repository.help.HelpCategoryRepository;
import com.shoppingmall.service.common.HelpService;
import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帮助中心服务实现类（前端使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("commonHelpService")
@RequiredArgsConstructor
public class HelpServiceImpl implements HelpService {

    private final HelpCategoryRepository categoryRepository;
    private final HelpArticleRepository articleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<HelpCategoryVO> getCategoryTree() {
        // 只查询启用的分类
        List<HelpCategory> categories = categoryRepository.selectList(
                new LambdaQueryWrapper<HelpCategory>()
                        .eq(HelpCategory::getStatus, 1)
                        .eq(HelpCategory::getDeleted, 0)
                        .orderByAsc(HelpCategory::getSort)
        );

        List<HelpCategoryVO> categoryVOList = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return buildCategoryTree(categoryVOList);
    }

    @Override
    public List<HelpArticleVO> getArticlesByCategoryId(Long categoryId) {
        // 只查询启用的文章
        List<HelpArticle> articles = articleRepository.selectList(
                new LambdaQueryWrapper<HelpArticle>()
                        .eq(HelpArticle::getCategoryId, categoryId)
                        .eq(HelpArticle::getStatus, 1)
                        .eq(HelpArticle::getDeleted, 0)
                        .orderByAsc(HelpArticle::getSort)
        );

        return articles.stream()
                .map(this::convertArticleToVO)
                .collect(Collectors.toList());
    }

    @Override
    public HelpArticleVO getArticleById(Long id) {
        HelpArticle article = articleRepository.selectById(id);
        if (article == null || article.getDeleted() == 1 || article.getStatus() == 0) {
            return null;
        }
        return convertArticleToVO(article);
    }

    /**
     * 转换为VO
     */
    private HelpCategoryVO convertToVO(HelpCategory category) {
        HelpCategoryVO vo = new HelpCategoryVO();
        BeanUtils.copyProperties(category, vo);
        return vo;
    }

    /**
     * 转换为文章VO
     */
    private HelpArticleVO convertArticleToVO(HelpArticle article) {
        HelpArticleVO vo = new HelpArticleVO();
        BeanUtils.copyProperties(article, vo);
        
        // 解析图片JSON
        if (article.getImages() != null && !article.getImages().isEmpty()) {
            try {
                List<String> images = objectMapper.readValue(article.getImages(), new TypeReference<List<String>>() {});
                vo.setImages(images);
            } catch (Exception e) {
                log.error("解析文章图片JSON失败: {}", article.getImages(), e);
                vo.setImages(new ArrayList<>());
            }
        } else {
            vo.setImages(new ArrayList<>());
        }
        
        return vo;
    }

    /**
     * 构建分类树
     */
    private List<HelpCategoryVO> buildCategoryTree(List<HelpCategoryVO> allCategories) {
        List<HelpCategoryVO> rootCategories = new ArrayList<>();
        
        for (HelpCategoryVO category : allCategories) {
            if (category.getParentId() == null || category.getParentId() == 0) {
                rootCategories.add(category);
            } else {
                // 找到父分类并添加为子分类
                for (HelpCategoryVO parent : allCategories) {
                    if (parent.getId().equals(category.getParentId())) {
                        if (parent.getChildren() == null) {
                            parent.setChildren(new ArrayList<>());
                        }
                        parent.getChildren().add(category);
                        break;
                    }
                }
            }
        }
        
        return rootCategories;
    }
}







































































