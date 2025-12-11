package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品服务实现类
 *
 * @author ShoppingMall Team
 * @date 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductCategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String status) {
        Page<Product> page = new Page<>(current, size);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 分类筛选
        if (categoryId != null) {
            wrapper.eq(Product::getCategoryId, categoryId);
        }

        // 关键词搜索
        if (StringUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Product::getProductName, keyword)
                    .or().like(Product::getProductCode, keyword));
        }

        // 状态筛选
        if (StringUtil.isNotBlank(status)) {
            wrapper.eq(Product::getStatus, status);
        }

        // 按销量降序
        wrapper.orderByDesc(Product::getSalesCount);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO
        Page<ProductVO> voPage = new Page<>();
        voPage.setCurrent(productPage.getCurrent());
        voPage.setSize(productPage.getSize());
        voPage.setTotal(productPage.getTotal());
        voPage.setRecords(productPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return voPage;
    }

    @Override
    public ProductVO getProductById(Long id) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return convertToVO(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createProduct(ProductDTO productDTO) {
        // 检查商品编码是否重复
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, productDTO.getProductCode());
        if (productRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "商品编码已存在");
        }

        // 验证分类是否存在
        ProductCategory category = categoryRepository.selectById(productDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "商品分类不存在");
        }

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product);

        if (product.getStatus() == null) {
            product.setStatus("下架");
        }
        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSalesCount() == null) {
            product.setSalesCount(0);
        }

        productRepository.insert(product);
        log.info("创建商品成功: {}", product.getProductName());
        return product.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductDTO productDTO) {
        Product product = productRepository.selectById(productDTO.getId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 检查商品编码是否重复（排除自己）
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getProductCode, productDTO.getProductCode())
                .ne(Product::getId, productDTO.getId());
        if (productRepository.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "商品编码已存在");
        }

        // 验证分类是否存在
        ProductCategory category = categoryRepository.selectById(productDTO.getCategoryId());
        if (category == null) {
            throw new BusinessException(400, "商品分类不存在");
        }

        BeanUtils.copyProperties(productDTO, product, "id", "salesCount");
        productRepository.updateById(product);
        log.info("更新商品成功: {}", product.getProductName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(Long id) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        productRepository.deleteById(id);
        log.info("删除商品成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, String status) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        product.setStatus(status);
        productRepository.updateById(product);
        log.info("更新商品状态成功: id={}, status={}", id, status);
    }

    @Override
    public Page<ProductVO> getHotProducts(Long limit) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "上架")
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT " + limit);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO
        Page<ProductVO> voPage = new Page<>();
        voPage.setRecords(productPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return voPage;
    }

    @Override
    public Page<ProductVO> getRecommendProducts(Long categoryId, Long limit) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, "上架")
                .orderByDesc(Product::getSalesCount)
                .last("LIMIT " + limit);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO
        Page<ProductVO> voPage = new Page<>();
        voPage.setRecords(productPage.getRecords().stream()
                .map(this::convertToVO)
                .toList());

        return voPage;
    }

    /**
     * 转换为VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);

        // 获取分类名称
        ProductCategory category = categoryRepository.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }

        // 解析图片JSON数组
        if (StringUtil.isNotBlank(product.getImages())) {
            try {
                List<String> imageList = objectMapper.readValue(product.getImages(), List.class);
                vo.setImageList(imageList);
            } catch (Exception e) {
                log.error("解析商品图片失败: productId={}", product.getId(), e);
                vo.setImageList(new ArrayList<>());
            }
        } else {
            vo.setImageList(new ArrayList<>());
        }

        // TODO: 根据用户等级获取对应价格
        vo.setUserLevelPrice(product.getBasePrice());

        return vo;
    }
}
