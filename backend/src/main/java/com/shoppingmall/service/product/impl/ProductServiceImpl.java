package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.ProductDTO;
import com.shoppingmall.entity.Brand;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductCategory;
import com.shoppingmall.dto.ProductMemberPriceDTO;
import com.shoppingmall.entity.ProductMemberPrice;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.entity.ProductSkuMemberPrice;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.product.ProductMemberPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.sku.ProductSkuMemberPriceRepository;
import com.shoppingmall.repository.user.UserRepository;
import com.shoppingmall.repository.website.BrandRepository;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.service.member.MemberLevelService;
import com.shoppingmall.service.product.ProductCategoryService;
import com.shoppingmall.service.product.ProductService;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.service.user.StockNotificationService;
import com.shoppingmall.event.ProductPublishedEvent;
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
    private final ProductCategoryService categoryService;
    private final BrandRepository brandRepository;
    private final ProductStockRepository productStockRepository;
    private final ObjectMapper objectMapper;
    private final StockNotificationService stockNotificationService;
    private final StockService stockService;
    private final MemberLevelService memberLevelService;
    private final UserRepository userRepository;
    private final ProductSkuService productSkuService;
    private final ProductMemberPriceRepository productMemberPriceRepository;
    private final ProductSkuMemberPriceRepository productSkuMemberPriceRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    @Override
    public Page<ProductVO> getProductPage(Long current, Long size, Long categoryId, String keyword, String brand,
            String status, String sortBy, Long userId, Boolean includeDeleted) {
        Page<Product> page = new Page<>(current, size);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        
        // MyBatis-Plus 的 @TableLogic 会自动过滤 deleted=1 的记录，只查询未删除的商品

        // 分类筛选（支持查询父分类及其所有子分类的商品）
        if (categoryId != null) {
            // 获取该分类及其所有子分类的ID列表
            List<Long> categoryIds = categoryService.getAllCategoryIdsIncludingChildren(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
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
            wrapper.eq(Product::getStatus, statusToInteger(status));
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
                    // 综合排序：先按销量降序，销量相同时按创建时间降序（新品优先）
                    wrapper.orderByDesc(Product::getSalesCount)
                           .orderByDesc(Product::getCreateTime);
                    break;
            }
        } else {
            // 默认综合排序：先按销量降序，销量相同时按创建时间降序
            wrapper.orderByDesc(Product::getSalesCount)
                   .orderByDesc(Product::getCreateTime);
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
        BeanUtils.copyProperties(productDTO, product, "status", "weight", "stock", "warningStock");

        // 状态映射：上架=1，下架=0，草稿=2
        product.setStatus(statusToInteger(productDTO.getStatus()));

        // 手动处理 weight 字段：BigDecimal 转 Integer（单位：克）
        if (productDTO.getWeight() != null) {
            product.setWeight(productDTO.getWeight().intValue());
        } else {
            product.setWeight(null);
        }

        // 手动设置预警库存字段（警戒库存），确保正确映射
        if (productDTO.getWarningStock() != null) {
            product.setWarningStock(productDTO.getWarningStock());
        } else {
            product.setWarningStock(0); // 默认值为0
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

        // 保存商品会员价列表
        saveProductMemberPrices(product.getId(), productDTO.getMemberPrices());

        // 新创建商品自动同步到聚水潭ERP
        syncToJushuitan(product);

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

        // 状态映射：上架=1，下架=0，草稿=2
        product.setStatus(statusToInteger(productDTO.getStatus()));

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

        // 保存商品会员价列表
        saveProductMemberPrices(product.getId(), productDTO.getMemberPrices());

        // 每次商品编辑保存都自动同步到聚水潭ERP
        syncToJushuitan(product);

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

        // 状态映射：上架=1，下架=0，草稿=2
        product.setStatus(statusToInteger(status));
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
        // 获取该分类及其所有子分类的ID列表（支持查询父分类及其所有子分类的商品）
        List<Long> categoryIds = categoryService.getAllCategoryIdsIncludingChildren(categoryId);
        wrapper.in(Product::getCategoryId, categoryIds)
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

        // 状态映射：1=上架，0=下架，2=草稿
        vo.setStatus(statusToString(product.getStatus()));

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

        // 0. 获取用户信息，判断是否是会员
        Integer isMember = 0;
        if (userId != null) {
            User user = userRepository.selectById(userId);
            isMember = (user != null && user.getIsMember() != null && user.getIsMember() == 1) ? 1 : 0;
        }
        vo.setIsMember(isMember);

        // 1. 计算商品会员价（参考购物车逻辑）
        BigDecimal salesPrice = product.getBasePrice();
        if (salesPrice == null) {
            salesPrice = BigDecimal.ZERO;
        }
        BigDecimal memberPrice = calculateMemberPriceForProduct(product, salesPrice, userId);
        vo.setMemberPrice(memberPrice);
        vo.setUserLevelPrice(salesPrice);

        // 1.1 加载商品会员价配置（管理后台编辑时使用）
        if (userId == null) { // 管理后台查询时userId为null
            try {
                LambdaQueryWrapper<ProductMemberPrice> memberPriceWrapper = new LambdaQueryWrapper<>();
                memberPriceWrapper.eq(ProductMemberPrice::getProductId, product.getId());
                List<ProductMemberPrice> productMemberPrices = productMemberPriceRepository.selectList(memberPriceWrapper);
                
                if (productMemberPrices != null && !productMemberPrices.isEmpty()) {
                    List<com.shoppingmall.vo.ProductMemberPriceVO> memberPriceVOs = productMemberPrices.stream()
                        .map(mp -> {
                            com.shoppingmall.vo.ProductMemberPriceVO mpVO = new com.shoppingmall.vo.ProductMemberPriceVO();
                            mpVO.setMemberLevelId(mp.getMemberLevelId());
                            mpVO.setMemberPrice(mp.getMemberPrice());
                            return mpVO;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    vo.setMemberPrices(memberPriceVOs);
                }
            } catch (Exception e) {
                log.error("加载商品会员价配置失败: productId={}", product.getId(), e);
            }
        }

        // 2. 获取 SKU 列表并处理 SKU 层级的会员价
        try {
            var skuList = productSkuService.getSkusByProductId(product.getId(), userId);
            if (skuList != null) {
                skuList.forEach(sku -> {
                    // SKU会员价逻辑：如果启用会员价，根据会员等级从 product_sku_member_price 表查询
                    BigDecimal skuSalesPrice = sku.getPrice();
                    if (skuSalesPrice == null) {
                        skuSalesPrice = BigDecimal.ZERO;
                    }
                    // 计算SKU会员价
                    BigDecimal skuMemberPrice = calculateMemberPriceForSku(sku, skuSalesPrice, userId);
                    sku.setMemberPrice(skuMemberPrice);
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
     * 计算商品的会员价格
     * 新逻辑：
     * 1. 普通用户：返回基础价格
     * 2. 会员用户：
     *    a. 如果启用会员价：根据会员等级从 product_member_price 表查询会员价
     *    b. 如果没有启用会员价：返回基础价格
     *
     * @param product 商品实体
     * @param salesPrice 销售价格（基础价格）
     * @param userId 用户ID
     * @return 会员价格（非会员或未启用会员价返回基础价格）
     */
    private BigDecimal calculateMemberPriceForProduct(Product product, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. 检查用户是否是会员
        User user = userId != null ? userRepository.selectById(userId) : null;
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 普通用户：返回基础价格
            return salesPrice;
        }

        // 2. 如果是会员，检查商品是否启用了会员价
        if (product.getEnableMemberPrice() == null || product.getEnableMemberPrice() != 1) {
            // 未启用会员价：返回基础价格
            return salesPrice;
        }

        // 3. 启用会员价：根据会员等级查询会员价
        Long memberLevelId = user.getMemberLevelId();
        if (memberLevelId == null) {
            // 会员但没有等级：返回基础价格
            return salesPrice;
        }

        try {
            LambdaQueryWrapper<ProductMemberPrice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductMemberPrice::getProductId, product.getId())
                   .eq(ProductMemberPrice::getMemberLevelId, memberLevelId);
            ProductMemberPrice productMemberPrice = productMemberPriceRepository.selectOne(wrapper);
            
            if (productMemberPrice != null && productMemberPrice.getMemberPrice() != null 
                    && productMemberPrice.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                return productMemberPrice.getMemberPrice();
            }
        } catch (Exception e) {
            log.error("查询商品会员价失败: productId={}, memberLevelId={}, userId={}", 
                    product.getId(), memberLevelId, userId, e);
        }

        // 如果查询不到会员价配置，返回基础价格
        return salesPrice;
    }

    /**
     * 计算SKU的会员价格
     * 新逻辑：
     * 1. 普通用户：返回基础价格
     * 2. 会员用户：
     *    a. 如果启用会员价：根据会员等级从 product_sku_member_price 表查询会员价
     *    b. 如果没有启用会员价：返回基础价格
     *
     * @param sku SKU实体（ProductSkuVO）
     * @param salesPrice 销售价格（基础价格）
     * @param userId 用户ID
     * @return 会员价格（非会员或未启用会员价返回基础价格）
     */
    private BigDecimal calculateMemberPriceForSku(com.shoppingmall.vo.ProductSkuVO sku, BigDecimal salesPrice, Long userId) {
        if (salesPrice == null || salesPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // 1. 检查用户是否是会员
        User user = userId != null ? userRepository.selectById(userId) : null;
        if (user == null || user.getIsMember() == null || user.getIsMember() != 1) {
            // 普通用户：返回基础价格
            return salesPrice;
        }

        // 2. 如果是会员，检查SKU是否启用了会员价
        if (sku.getEnableMemberPrice() == null || sku.getEnableMemberPrice() != 1) {
            // 未启用会员价：返回基础价格
            return salesPrice;
        }

        // 3. 启用会员价：根据会员等级查询会员价
        Long memberLevelId = user.getMemberLevelId();
        if (memberLevelId == null || sku.getId() == null) {
            // 会员但没有等级或SKU没有ID：返回基础价格
            return salesPrice;
        }

        try {
            LambdaQueryWrapper<ProductSkuMemberPrice> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductSkuMemberPrice::getSkuId, sku.getId())
                   .eq(ProductSkuMemberPrice::getMemberLevelId, memberLevelId);
            ProductSkuMemberPrice skuMemberPrice = productSkuMemberPriceRepository.selectOne(wrapper);
            
            if (skuMemberPrice != null && skuMemberPrice.getMemberPrice() != null 
                    && skuMemberPrice.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                return skuMemberPrice.getMemberPrice();
            }
        } catch (Exception e) {
            log.error("查询SKU会员价失败: skuId={}, memberLevelId={}, userId={}", 
                    sku.getId(), memberLevelId, userId, e);
        }

        // 如果查询不到会员价配置，返回基础价格
        return salesPrice;
    }

    /**
     * 根据会员等级折扣率计算会员价格
     * 已废弃：不再使用折扣率计算会员价，改为从 product_member_price 和 product_sku_member_price 表查询
     * 
     * @deprecated 此方法已废弃，不再使用折扣率计算会员价
     */
    @Deprecated
    private BigDecimal calculateMemberPriceByDiscount(BigDecimal salesPrice, Long userId) {
        // 已废弃，直接返回基础价格
        return salesPrice;
    }

    /**
     * 将字符串状态转换为整数
     * @param status 状态字符串：上架、下架、草稿
     * @return 状态整数：1=上架，0=下架，2=草稿
     */
    private Integer statusToInteger(String status) {
        if (status == null) {
            return 0; // 默认下架
        }
        switch (status) {
            case "上架":
                return 1;
            case "草稿":
                return 2;
            case "下架":
            default:
                return 0;
        }
    }

    /**
     * 将整数状态转换为字符串
     * @param status 状态整数：1=上架，0=下架，2=草稿
     * @return 状态字符串
     */
    private String statusToString(Integer status) {
        if (status == null) {
            return "下架";
        }
        switch (status) {
            case 1:
                return "上架";
            case 2:
                return "草稿";
            case 0:
            default:
                return "下架";
        }
    }

    // 已删除原createOrUpdateProductStock方法，统一使用StockService.updateProductTotalStock

    /**
     * 保存商品会员价列表
     *
     * @param productId 商品ID
     * @param memberPrices 会员价列表
     */
    private void saveProductMemberPrices(Long productId, List<ProductMemberPriceDTO> memberPrices) {
        if (productId == null) {
            return;
        }

        // 删除该商品的所有现有会员价配置
        LambdaQueryWrapper<ProductMemberPrice> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(ProductMemberPrice::getProductId, productId);
        productMemberPriceRepository.delete(deleteWrapper);

        // 如果有新的会员价配置，则保存
        if (memberPrices != null && !memberPrices.isEmpty()) {
            for (ProductMemberPriceDTO dto : memberPrices) {
                if (dto.getMemberLevelId() != null && dto.getMemberPrice() != null 
                        && dto.getMemberPrice().compareTo(BigDecimal.ZERO) > 0) {
                    ProductMemberPrice entity = new ProductMemberPrice();
                    entity.setProductId(productId);
                    entity.setMemberLevelId(dto.getMemberLevelId());
                    entity.setMemberPrice(dto.getMemberPrice());
                    productMemberPriceRepository.insert(entity);
                }
            }
            log.info("保存商品{}会员价配置成功，共{}个等级", productId, memberPrices.size());
        }
    }

    /**
     * 同步商品到聚水潭ERP
     * 通过发布事件的方式异步处理，避免循环依赖
     */
    private void syncToJushuitan(Product product) {
        try {
            log.info("商品{}保存操作，发布同步事件", product.getId());

            // 发布商品保存事件，由事件监听器异步处理同步
            ProductPublishedEvent event = new ProductPublishedEvent(
                this,
                product.getId(),
                product.getProductCode(),
                product.getProductName()
            );
            eventPublisher.publishEvent(event);

        } catch (Exception e) {
            log.error("发布商品{}同步事件失败", product.getId(), e);
            // 不影响商品保存的主流程
        }
    }
}
