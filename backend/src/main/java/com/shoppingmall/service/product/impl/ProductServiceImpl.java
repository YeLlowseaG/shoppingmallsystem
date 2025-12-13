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
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.service.admin.StockService;
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
    private final StockNotificationService stockNotificationService;
    private final StockService stockService;

    @Override
    public Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String brand, String status, String sortBy) {
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

        // 动态排序
        if (StringUtil.isNotBlank(sortBy)) {
            switch (sortBy) {
                case "price_asc":
                    wrapper.orderByAsc(Product::getBasePrice);
                    break;
                case "price_desc":
                    wrapper.orderByDesc(Product::getBasePrice);
                    break;
                case "stock_asc":
                    wrapper.orderByAsc(Product::getStock);
                    break;
                case "stock_desc":
                    wrapper.orderByDesc(Product::getStock);
                    break;
                case "sales_asc":
                    wrapper.orderByAsc(Product::getSalesCount);
                    break;
                case "sales_desc":
                    wrapper.orderByDesc(Product::getSalesCount);
                    break;
                case "create_time_asc":
                    wrapper.orderByAsc(Product::getCreateTime);
                    break;
                case "create_time_desc":
                    wrapper.orderByDesc(Product::getCreateTime);
                    break;
                case "default":
                default:
                    wrapper.orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            wrapper.orderByDesc(Product::getCreateTime);
        }

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
        
        // 如果指定了库存，使用StockService统一管理
        if (product.getStock() != null && product.getStock() >= 0) {
            try {
                stockService.updateProductTotalStock(product.getId(), product.getStock());
            } catch (Exception e) {
                log.error("创建商品{}库存记录失败", product.getId(), e);
                // 创建商品时库存记录失败不影响主流程，只记录日志
            }
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

        // 检查库存变化，用于缺货通知
        Integer oldStock = product.getStock();
        boolean wasOutOfStock = (oldStock == null || oldStock <= 0);

        BeanUtils.copyProperties(productDTO, product, "id", "salesCount", "status");

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(productDTO.getStatus()) ? 1 : 0);

        productRepository.updateById(product);
        
        // 如果更新了库存，使用StockService统一管理
        if (productDTO.getStock() != null) {
            try {
                stockService.updateProductTotalStock(product.getId(), productDTO.getStock());
                
                // 检查是否从缺货状态变为有库存状态，如果是则发送通知
                boolean isNowInStock = productDTO.getStock() > 0;
                if (wasOutOfStock && isNowInStock) {
                    try {
                        stockNotificationService.notifyUsers(product.getId());
                        log.info("商品{}补货通知已发送", product.getId());
                    } catch (Exception e) {
                        log.error("发送商品{}补货通知失败", product.getId(), e);
                        // 不影响商品更新的主流程
                    }
                }
            } catch (Exception e) {
                log.error("更新商品{}库存失败", product.getId(), e);
                throw new BusinessException(500, "更新商品库存失败");
            }
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

    // 已删除原createOrUpdateProductStock方法，统一使用StockService.updateProductTotalStock
}
