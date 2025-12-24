package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.repository.website.BrandRepository;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.vo.MemberLevelVO;
import com.shoppingmall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final MemberLevelService memberLevelService;
    private final UserRepository userRepository;
    private final ProductSkuService productSkuService;

    @Override
    public Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String brand,
            String status, String sortBy, Long userId) {
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
                .map(p -> convertToVO(p, userId))
                .toList());

        return voPage;
    }

    @Override
    public ProductVO getProductById(Long id, Long userId) {
        Product product = productRepository.selectById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return convertToVO(product, userId);
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
        BeanUtils.copyProperties(productDTO, product, "status", "weight", "stock");

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(productDTO.getStatus()) ? 1 : 0);

        // 手动处理 weight 字段：BigDecimal 转 Integer（单位：克）
        if (productDTO.getWeight() != null) {
            product.setWeight(productDTO.getWeight().intValue());
        } else {
            product.setWeight(null);
        }

        // 库存处理：如果启用规格，库存设为0（稍后由SKU创建时自动计算总和）
        // 如果不启用规格，使用用户输入的库存
        if (productDTO.getEnableSpec() != null && productDTO.getEnableSpec() == 1) {
            product.setStock(0);
            log.info("商品启用规格，初始库存设为0，等待SKU创建后自动计算");
        } else {
            product.setStock(productDTO.getStock() != null ? productDTO.getStock() : 0);
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

        BeanUtils.copyProperties(productDTO, product, "id", "salesCount", "status", "weight", "stock");

        // 状态映射：上架=1，下架=0
        product.setStatus("上架".equals(productDTO.getStatus()) ? 1 : 0);

        // 手动处理 weight 字段：BigDecimal 转 Integer（单位：克）
        if (productDTO.getWeight() != null) {
            product.setWeight(productDTO.getWeight().intValue());
        } else {
            product.setWeight(null);
        }

        // 库存处理：如果启用规格，从SKU计算总库存；如果不启用规格，使用用户输入的库存
        Integer calculatedStock;
        if (productDTO.getEnableSpec() != null && productDTO.getEnableSpec() == 1) {
            // 启用规格时，从SKU计算总库存
            var skuList = productSkuService.getSkusByProductId(product.getId(), null);
            calculatedStock = skuList != null ? skuList.stream()
                    .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                    .sum() : 0;
            product.setStock(calculatedStock);
            log.info("商品启用规格，从SKU计算总库存：{}", calculatedStock);
        } else {
            // 不启用规格时，使用用户输入的库存
            calculatedStock = productDTO.getStock() != null ? productDTO.getStock() : product.getStock();
            product.setStock(calculatedStock);
        }

        productRepository.updateById(product);

        // 如果更新了库存，使用StockService统一管理
        if (calculatedStock != null) {
            try {
                stockService.updateProductTotalStock(product.getId(), calculatedStock);

                // 检查是否从缺货状态变为有库存状态，如果是则发送通知
                boolean isNowInStock = calculatedStock > 0;
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
    public List<ProductVO> getHotProducts(Long limit, Long userId) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1) // 1=上架
                .orderByDesc(Product::getSalesCount);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO并返回List
        return productPage.getRecords().stream()
                .map(p -> convertToVO(p, userId))
                .toList();
    }

    @Override
    public List<ProductVO> getRecommendProducts(Long categoryId, Long limit, Long userId) {
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getCategoryId, categoryId)
                .eq(Product::getStatus, 1) // 1=上架
                .orderByDesc(Product::getSalesCount);

        Page<Product> productPage = productRepository.selectPage(page, wrapper);

        // 转换为VO并返回List
        return productPage.getRecords().stream()
                .map(p -> convertToVO(p, userId))
                .toList();
    }

    /**
     * 转换为VO
     */
    private ProductVO convertToVO(Product product, Long userId) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo, "status", "weight");

        // 状态映射：1=上架，0=下架
        vo.setStatus(product.getStatus() == 1 ? "上架" : "下架");

        // 手动处理 weight 字段：Integer 转 BigDecimal（单位：克）
        if (product.getWeight() != null) {
            vo.setWeight(BigDecimal.valueOf(product.getWeight()));
        } else {
            vo.setWeight(null);
        }

        // 获取分类名称
        ProductCategory category = categoryRepository.selectById(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }

        // 获取品牌名称
        if (product.getBrandId() != null) {
            Brand brand = brandRepository.selectById(product.getBrandId());
            if (brand != null) {
                vo.setBrandName(brand.getBrandName());
            }
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

        // 会员价逻辑：如果启用了固定会员价且有值，使用固定会员价；否则按等级折扣计算
        if (product.getEnableMemberPrice() != null && product.getEnableMemberPrice() == 1
                && product.getMemberPrice() != null && product.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
            // 使用商品设置的固定会员价（已通过BeanUtils复制到vo）
            vo.setUserLevelPrice(product.getMemberPrice());
        } else {
            // 按用户等级折扣计算会员价
            vo.setMemberPrice(calculateMemberPrice(product.getBasePrice(), userId));
            vo.setUserLevelPrice(product.getBasePrice());
        }

        // 获取 SKU 列表并处理 SKU 层级的会员价
        try {
            var skuList = productSkuService.getSkusByProductId(product.getId(), userId);
            if (skuList != null) {
                skuList.forEach(sku -> {
                    // SKU会员价逻辑：如果SKU启用了固定会员价且有值，使用固定会员价；否则按等级折扣计算
                    if (sku.getEnableMemberPrice() != null && sku.getEnableMemberPrice() == 1
                            && sku.getMemberPrice() != null && sku.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                        // SKU已设置固定会员价，保留原值（已从数据库读取）
                    } else {
                        // 按用户等级折扣计算
                        sku.setMemberPrice(calculateMemberPrice(sku.getPrice(), userId));
                    }
                });
            }
            vo.setSkus(skuList);
        } catch (Exception e) {
            log.error("获取或计算 SKU 会员价失败: productId={}, userId={}", product.getId(), userId, e);
            vo.setSkus(new ArrayList<>());
        }

        return vo;
    }

    /**
     * 根据会员等级折扣率计算会员价格
     *
     * @param salesPrice 销售价
     * @param userId     用户ID（可为空，未登录返回原价）
     * @return 会员价
     */
    private BigDecimal calculateMemberPrice(BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return salesPrice == null ? BigDecimal.ZERO : salesPrice;
        }

        try {
            // 获取所有启用的会员等级（按排序号排序）
            List<MemberLevelVO> memberLevels = memberLevelService.getAllEnabledMemberLevels();
            if (memberLevels == null || memberLevels.isEmpty()) {
                return salesPrice;
            }

            // 获取用户的会员等级ID
            Long memberLevelId = null;
            if (userId != null) {
                User user = userRepository.selectById(userId);
                // 只有会员才有会员等级
                if (user != null && user.getIsMember() != null && user.getIsMember() == 1 && user.getMemberLevelId() != null) {
                    memberLevelId = user.getMemberLevelId();
                }
            }

            // 查找匹配的会员等级
            MemberLevelVO memberLevel = null;
            if (memberLevelId != null) {
                for (MemberLevelVO level : memberLevels) {
                    if (level.getId() != null && level.getId().equals(memberLevelId)) {
                        memberLevel = level;
                        break;
                    }
                }
            }

            // 如果找不到匹配等级，使用第一个等级（默认）
            if (memberLevel == null) {
                memberLevel = memberLevels.get(0);
            }

            if (memberLevel == null || memberLevel.getDiscountRate() == null) {
                return salesPrice;
            }

            // 会员价 = 销售价 * (折扣率 / 100)
            return salesPrice.multiply(memberLevel.getDiscountRate())
                    .divide(new BigDecimal("100.00"), 2, BigDecimal.ROUND_HALF_UP);
        } catch (Exception e) {
            log.error("计算会员价格失败: userId={}, salesPrice={}", userId, salesPrice, e);
            return salesPrice;
        }
    }

    // 已删除原createOrUpdateProductStock方法，统一使用StockService.updateProductTotalStock
}
