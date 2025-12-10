package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.dto.HelpArticleDTO;
import com.shoppingmall.dto.HelpCategoryDTO;
import com.shoppingmall.entity.HelpArticle;
import com.shoppingmall.entity.HelpCategory;
import com.shoppingmall.repository.help.HelpArticleRepository;
import com.shoppingmall.repository.help.HelpCategoryRepository;
import com.shoppingmall.service.admin.HelpService;
import com.shoppingmall.vo.HelpArticleVO;
import com.shoppingmall.vo.HelpCategoryVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帮助中心管理服务实现类（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("adminHelpService")
@RequiredArgsConstructor
public class HelpServiceImpl implements HelpService {

    private final HelpCategoryRepository categoryRepository;
    private final HelpArticleRepository articleRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<HelpCategoryVO> getCategoryTree() {
        // 查询所有分类（包括禁用的）
        List<HelpCategory> categories = categoryRepository.selectList(
                new LambdaQueryWrapper<HelpCategory>()
                        .eq(HelpCategory::getDeleted, 0)
                        .orderByAsc(HelpCategory::getSort)
        );

        List<HelpCategoryVO> categoryVOList = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return buildCategoryTree(categoryVOList);
    }

    @Override
    public HelpCategoryVO getCategoryById(Long id) {
        HelpCategory category = categoryRepository.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        return convertToVO(category);
    }

    @Override
    @Transactional
    public void addCategory(HelpCategoryDTO categoryDTO) {
        HelpCategory category = new HelpCategory();
        BeanUtils.copyProperties(categoryDTO, category);
        categoryRepository.insert(category);
    }

    @Override
    @Transactional
    public void updateCategory(Long id, HelpCategoryDTO categoryDTO) {
        HelpCategory category = categoryRepository.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        
        BeanUtils.copyProperties(categoryDTO, category);
        category.setId(id);
        categoryRepository.updateById(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        HelpCategory category = categoryRepository.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有子分类
        long childCount = categoryRepository.selectCount(
                new LambdaQueryWrapper<HelpCategory>()
                        .eq(HelpCategory::getParentId, id)
                        .eq(HelpCategory::getDeleted, 0)
        );
        if (childCount > 0) {
            throw new BusinessException("该分类下存在子分类，无法删除");
        }
        
        // 检查是否有文章
        long articleCount = articleRepository.selectCount(
                new LambdaQueryWrapper<HelpArticle>()
                        .eq(HelpArticle::getCategoryId, id)
                        .eq(HelpArticle::getDeleted, 0)
        );
        if (articleCount > 0) {
            throw new BusinessException("该分类下存在文章，无法删除");
        }
        
        categoryRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateCategoryStatus(Long id, Integer status) {
        HelpCategory category = categoryRepository.selectById(id);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        category.setStatus(status);
        categoryRepository.updateById(category);
    }

    @Override
    public IPage<HelpArticleVO> getArticleList(Integer pageNum, Integer pageSize, Long categoryId, String title, Integer status) {
        Page<HelpArticle> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<HelpArticle> wrapper = new LambdaQueryWrapper<HelpArticle>()
                .eq(HelpArticle::getDeleted, 0);
        
        if (categoryId != null) {
            wrapper.eq(HelpArticle::getCategoryId, categoryId);
        }
        if (title != null && !title.isEmpty()) {
            wrapper.like(HelpArticle::getTitle, title);
        }
        if (status != null) {
            wrapper.eq(HelpArticle::getStatus, status);
        }
        
        wrapper.orderByAsc(HelpArticle::getSort)
               .orderByDesc(HelpArticle::getCreateTime);
        
        IPage<HelpArticle> articlePage = articleRepository.selectPage(page, wrapper);
        
        // 转换为VO
        IPage<HelpArticleVO> voPage = articlePage.convert(this::convertArticleToVO);
        
        // 填充分类名称
        for (HelpArticleVO vo : voPage.getRecords()) {
            HelpCategory category = categoryRepository.selectById(vo.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        
        return voPage;
    }

    @Override
    public HelpArticleVO getArticleById(Long id) {
        HelpArticle article = articleRepository.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        
        HelpArticleVO vo = convertArticleToVO(article);
        
        // 填充分类名称
        HelpCategory category = categoryRepository.selectById(vo.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        return vo;
    }

    @Override
    @Transactional
    public void addArticle(HelpArticleDTO articleDTO) {
        // 检查分类是否存在
        HelpCategory category = categoryRepository.selectById(articleDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        
        HelpArticle article = new HelpArticle();
        BeanUtils.copyProperties(articleDTO, article);
        
        // 转换图片列表为JSON
        if (articleDTO.getImages() != null && !articleDTO.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(articleDTO.getImages());
                article.setImages(imagesJson);
            } catch (Exception e) {
                log.error("转换文章图片为JSON失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        }
        
        articleRepository.insert(article);
    }

    @Override
    @Transactional
    public void updateArticle(Long id, HelpArticleDTO articleDTO) {
        HelpArticle article = articleRepository.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        
        // 检查分类是否存在
        HelpCategory category = categoryRepository.selectById(articleDTO.getCategoryId());
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException("分类不存在");
        }
        
        BeanUtils.copyProperties(articleDTO, article);
        article.setId(id);
        
        // 转换图片列表为JSON
        if (articleDTO.getImages() != null && !articleDTO.getImages().isEmpty()) {
            try {
                String imagesJson = objectMapper.writeValueAsString(articleDTO.getImages());
                article.setImages(imagesJson);
            } catch (Exception e) {
                log.error("转换文章图片为JSON失败", e);
                throw new BusinessException("图片数据格式错误");
            }
        } else {
            article.setImages(null);
        }
        
        articleRepository.updateById(article);
    }

    @Override
    @Transactional
    public void deleteArticle(Long id) {
        HelpArticle article = articleRepository.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        articleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void updateArticleStatus(Long id, Integer status) {
        HelpArticle article = articleRepository.selectById(id);
        if (article == null || article.getDeleted() == 1) {
            throw new BusinessException("文章不存在");
        }
        article.setStatus(status);
        articleRepository.updateById(article);
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

