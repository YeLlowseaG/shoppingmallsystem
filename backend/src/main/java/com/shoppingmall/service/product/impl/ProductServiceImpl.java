package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.website.BrandRepository;
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
    private final BrandRepository brandRepository;
    private final ProductStockRepository productStockRepository;
    private final ObjectMapper objectMapper;

    @Override
    public Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String brand, String status) {
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

        // 品牌筛选
        if (StringUtil.isNotBlank(brand)) {
            // 先通过品牌名称查找品牌ID
            LambdaQueryWrapper<Brand> brandWrapper = new LambdaQueryWrapper<>();
            brandWrapper.eq(Brand::getBrandName, brand);
            Brand brandEntity = brandRepository.selectOne(brandWrapper);
            if (brandEntity != null) {
                wrapper.eq(Product::getBrandId, brandEntity.getId());
            } else {
                // 如果品牌不存在，返回空结果
                wrapper.eq(Product::getId, -1);
            }
        }

        // 状态筛选
        if (StringUtil.isNotBlank(status)) {
            wrapper.eq(Product::getStatus, "上架".equals(status) ? 1 : 0);
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
        BeanUtils.copyProperties(productDTO, product, "status");

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(productDTO.getStatus()) ? 1 : 0);

        if (product.getStock() == null) {
            product.setStock(0);
        }
        if (product.getSalesCount() == null) {
            product.setSalesCount(0);
        }

        productRepository.insert(product);
        
        // 如果指定了库存，创建或更新 product_stock 记录
        if (product.getStock() != null && product.getStock() > 0) {
            createOrUpdateProductStock(product.getId(), product.getStock());
        }
        
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

        BeanUtils.copyProperties(productDTO, product, "id", "salesCount", "status");

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(productDTO.getStatus()) ? 1 : 0);

        productRepository.updateById(product);
        
        // 如果更新了库存，同步更新 product_stock 记录
        if (productDTO.getStock() != null) {
            createOrUpdateProductStock(product.getId(), productDTO.getStock());
        }
        
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

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(status) ? 1 : 0);
        productRepository.updateById(product);
        log.info("更新商品状态成功: id={}, status={}", id, status);
    }

    @Override
    public List<ProductVO> getHotProducts(Long limit) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)  // 1=上架
                .orderByDesc(Product::getSalesCount);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO并返回List
        return productPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
    }

    @Override
    public List<ProductVO> getRecommendProducts(Long categoryId, Long limit) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, 1)  // 1=上架
                .orderByDesc(Product::getSalesCount);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO并返回List
        return productPage.getRecords().stream()
                .map(this::convertToVO)
                .toList();
    }

    /**
     * 转换为VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo, "status");

        // 状态映射：1=上架，0=下架
        vo.setStatus(product.getStatus() == 1 ? "上架" : "下架");

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

    /**
     * 创建或更新 product_stock 记录
     * 以 product.stock 为数据源，同步到 product_stock.total_stock
     * 
     * @param productId 商品ID
     * @param totalStock 总库存
     */
    private void createOrUpdateProductStock(Long productId, Integer totalStock) {
        try {
            LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductStock::getProductId, productId);
            ProductStock stock = productStockRepository.selectOne(wrapper);
            
            if (stock == null) {
                // 创建新记录
                stock = new ProductStock();
                stock.setProductId(productId);
                stock.setTotalStock(totalStock);
                stock.setAvailableStock(totalStock);
                stock.setLockedStock(0);
                stock.setWarningThreshold(10);
                productStockRepository.insert(stock);
                log.debug("创建库存记录成功，商品ID: {}, 库存: {}", productId, totalStock);
            } else {
                // 更新现有记录
                int oldTotalStock = stock.getTotalStock() != null ? stock.getTotalStock() : 0;
                int adjustQuantity = totalStock - oldTotalStock;
                
                stock.setTotalStock(totalStock);
                // 可用库存 = 总库存 - 锁定库存
                int lockedStock = stock.getLockedStock() != null ? stock.getLockedStock() : 0;
                stock.setAvailableStock(totalStock - lockedStock);
                productStockRepository.updateById(stock);
                log.debug("更新库存记录成功，商品ID: {}, 库存: {} (调整: {})", productId, totalStock, adjustQuantity);
            }
        } catch (Exception e) {
            log.error("同步库存记录失败，商品ID: {}, 库存: {}", productId, totalStock, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}
