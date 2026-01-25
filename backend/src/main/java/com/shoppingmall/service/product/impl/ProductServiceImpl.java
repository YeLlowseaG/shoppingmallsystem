package com.shoppingmall.service.product.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import com.shoppingmall.entity.User;
import com.shoppingmall.repository.product.ProductCategoryRepository;
import com.shoppingmall.repository.product.ProductMemberPriceRepository;
import com.shoppingmall.repository.product.ProductRepository;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
        long startTime = System.currentTimeMillis();
        
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

        // 优化：排除 description 字段，提升查询性能（大字段）
        wrapper.select(Product::getId, Product::getProductCode, Product::getBarcode, Product::getUnit,
                Product::getProductName, Product::getCategoryId, Product::getBrandId,
                Product::getShippingTemplateId, Product::getMainImage, Product::getImages,
                Product::getBasePrice, Product::getSuggestedRetailPrice, Product::getMarketRetailPrice,
                Product::getMemberPrice, Product::getEnableMemberPrice,
                Product::getStock, Product::getWarningStock, Product::getWeight,
                Product::getStatus, Product::getSalesCount, Product::getEnableSpec,
                Product::getCreateTime, Product::getUpdateTime);
        // 明确不包含 description 字段

        long queryStartTime = System.currentTimeMillis();
        Page<Product> productPage = productRepository.selectPage(page, wrapper);
        long queryTime = System.currentTimeMillis() - queryStartTime;
        log.info("商品分页查询耗时: {}ms, 查询到{}条记录", queryTime, productPage.getRecords().size());

        List<Product> products = productPage.getRecords();
        if (products.isEmpty()) {
            Page<ProductVO> emptyPage = new Page<>();
            emptyPage.setCurrent(productPage.getCurrent());
            emptyPage.setSize(productPage.getSize());
            emptyPage.setTotal(productPage.getTotal());
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }

        // 批量查询优化：解决 N+1 查询问题
        long batchQueryStartTime = System.currentTimeMillis();
        
        // 1. 批量查询分类
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ProductCategory> categoryMap = categoryIds.isEmpty() ? 
                Collections.emptyMap() : 
                categoryRepository.selectBatchIds(categoryIds)
                        .stream()
                        .collect(Collectors.toMap(ProductCategory::getId, c -> c, (a, b) -> a));

        // 2. 批量查询品牌
        Set<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Brand> brandMap = brandIds.isEmpty() ? 
                Collections.emptyMap() : 
                brandRepository.selectBatchIds(brandIds)
                        .stream()
                        .collect(Collectors.toMap(Brand::getId, b -> b, (a, b) -> a));

        // 3. 查询用户信息（如果 userId 不为 null，只查询一次）
        User user = null;
        if (userId != null) {
            user = userRepository.selectById(userId);
        }
        final User finalUser = user;

        // 4. 批量查询商品会员价配置（仅管理后台需要，userId 为 null）
        final Map<Long, List<ProductMemberPrice>> productMemberPriceMap;
        if (userId == null) {
            Set<Long> productIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                LambdaQueryWrapper<ProductMemberPrice> memberPriceWrapper = new LambdaQueryWrapper<>();
                memberPriceWrapper.in(ProductMemberPrice::getProductId, productIds);
                List<ProductMemberPrice> allMemberPrices = productMemberPriceRepository.selectList(memberPriceWrapper);
                productMemberPriceMap = allMemberPrices.stream()
                        .collect(Collectors.groupingBy(ProductMemberPrice::getProductId));
            } else {
                productMemberPriceMap = Collections.emptyMap();
            }
        } else {
            productMemberPriceMap = Collections.emptyMap();
        }

        // 5. 批量查询 SKU（如果商品启用了规格）
        Map<Long, List<ProductSku>> skuMap = Collections.emptyMap();
        Set<Long> productsWithSpec = products.stream()
                .filter(p -> p.getEnableSpec() != null && p.getEnableSpec() == 1)
                .map(Product::getId)
                .collect(Collectors.toSet());
        if (!productsWithSpec.isEmpty()) {
            // 通过 ProductSkuService 批量查询，但需要检查是否有批量查询方法
            // 如果没有，需要添加批量查询方法或使用 repository
            try {
                // 这里暂时保持原有逻辑，后续可以进一步优化
                // 由于 getSkusByProductId 内部可能还有复杂逻辑，先保持现状
                skuMap = Collections.emptyMap(); // 暂时为空，在 convertToVOWithCache 中按需查询
            } catch (Exception e) {
                log.warn("批量查询SKU失败，将使用按需查询: {}", e.getMessage());
            }
        }

        long batchQueryTime = System.currentTimeMillis() - batchQueryStartTime;
        log.info("批量查询关联数据耗时: {}ms (分类: {}, 品牌: {}, 用户: {})", 
                batchQueryTime, categoryMap.size(), brandMap.size(), finalUser != null ? 1 : 0);

        // 转换为VO（使用批量查询的数据）
        long convertStartTime = System.currentTimeMillis();
        Page<ProductVO> voPage = new Page<>();
        voPage.setCurrent(productPage.getCurrent());
        voPage.setSize(productPage.getSize());
        voPage.setTotal(productPage.getTotal());
        voPage.setRecords(products.stream()
                .map(p -> {
                    ProductVO vo = convertToVOWithCache(p, finalUser, categoryMap, brandMap, productMemberPriceMap);
                    vo.setDescription(null); // 列表接口不返回description字段（双重保险）
                    return vo;
                })
                .toList());
        long convertTime = System.currentTimeMillis() - convertStartTime;
        log.info("VO转换耗时: {}ms", convertTime);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("商品分页查询总耗时: {}ms (查询: {}ms, 批量查询: {}ms, 转换: {}ms)", 
                totalTime, queryTime, batchQueryTime, convertTime);

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

        // 分离description字段，只有在明确传递且不为空时才处理
        // 如果description为null或空字符串，说明前端没有传递或不需要更新description字段
        String description = productDTO.getDescription();
        // 如果description为空字符串，也视为null，不更新description字段
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }
        productDTO.setDescription(null); // 临时移除description，避免同步保存大字段

        Product product = new Product();
        BeanUtils.copyProperties(productDTO, product, "status", "weight", "stock", "warningStock", "description");

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

        // 创建商品时，如果description不为空则设置，否则设置为null
        if (description != null && !description.trim().isEmpty()) {
            product.setDescription(description);
        } else {
            product.setDescription(null);
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

        // 更新description字段（如果存在且不为空）
        // 注意：只有当description字段明确传递且不为空时才更新，避免不必要的更新操作
        if (description != null && !description.trim().isEmpty()) {
            product.setDescription(description);
            productRepository.updateById(product);
            log.debug("商品{}的description字段已更新", product.getId());
        }

        // 移除：新创建商品自动同步到聚水潭ERP
        // 改为由前端统一通过 /api/admin/inventory/sync/{productId}/full 接口触发同步
        // syncToJushuitan(product);

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

        // 分离description字段，只有在明确传递且不为空时才处理
        // 如果description为null或空字符串，说明前端没有传递或不需要更新description字段
        String description = productDTO.getDescription();
        // 如果description为空字符串，也视为null，不更新description字段
        if (description != null && description.trim().isEmpty()) {
            description = null;
        }
        productDTO.setDescription(null); // 临时移除description，避免同步保存大字段

        BeanUtils.copyProperties(productDTO, product, "id", "salesCount", "status", "weight", "stock", "description");

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

        // 使用LambdaUpdateWrapper只更新指定字段
        LambdaUpdateWrapper<Product> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Product::getId, product.getId())
                .set(Product::getProductCode, product.getProductCode())
                .set(Product::getBarcode, product.getBarcode())
                .set(Product::getUnit, product.getUnit())
                .set(Product::getProductName, product.getProductName())
                .set(Product::getCategoryId, product.getCategoryId())
                .set(Product::getBrandId, product.getBrandId())
                .set(Product::getShippingTemplateId, product.getShippingTemplateId())
                .set(Product::getMainImage, product.getMainImage())
                .set(Product::getImages, product.getImages())
                .set(Product::getBasePrice, product.getBasePrice())
                .set(Product::getSalePrice, product.getSalePrice())
                .set(Product::getSuggestedRetailPrice, product.getSuggestedRetailPrice())
                .set(Product::getMarketRetailPrice, product.getMarketRetailPrice())
                .set(Product::getMemberPrice, product.getMemberPrice())
                .set(Product::getEnableMemberPrice, product.getEnableMemberPrice())
                .set(Product::getStock, product.getStock())
                .set(Product::getWarningStock, product.getWarningStock())
                .set(Product::getWeight, product.getWeight())
                .set(Product::getStatus, product.getStatus())
                .set(Product::getEnableSpec, product.getEnableSpec());
        
        // 只有当description不为null且不为空时，才更新description字段
        // 这样SKU更新接口调用商品更新时，如果description为null，就不会触发description字段更新
        if (description != null && !description.trim().isEmpty()) {
            updateWrapper.set(Product::getDescription, description);
        }
        
        productRepository.update(updateWrapper);

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

        // 移除：每次商品编辑保存都自动同步到聚水潭ERP
        // 改为由前端统一通过 /api/admin/inventory/sync/{productId}/full 接口触发同步
        // syncToJushuitan(product);

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
        long startTime = System.currentTimeMillis();
        
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1) // 1=上架
                .orderByDesc(Product::getSalesCount);

        // 优化：排除 description 字段，提升查询性能（大字段）
        wrapper.select(Product::getId, Product::getProductCode, Product::getBarcode, Product::getUnit,
                Product::getProductName, Product::getCategoryId, Product::getBrandId,
                Product::getShippingTemplateId, Product::getMainImage, Product::getImages,
                Product::getBasePrice, Product::getSuggestedRetailPrice, Product::getMarketRetailPrice,
                Product::getMemberPrice, Product::getEnableMemberPrice,
                Product::getStock, Product::getWarningStock, Product::getWeight,
                Product::getStatus, Product::getSalesCount, Product::getEnableSpec,
                Product::getCreateTime, Product::getUpdateTime);
        // 明确不包含 description 字段

        long queryStartTime = System.currentTimeMillis();
        Page<Product> productPage = productRepository.selectPage(page, wrapper);
        long queryTime = System.currentTimeMillis() - queryStartTime;
        log.info("热门商品查询耗时: {}ms, 查询到{}条记录", queryTime, productPage.getRecords().size());

        List<Product> products = productPage.getRecords();
        if (products.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询优化：解决 N+1 查询问题
        long batchQueryStartTime = System.currentTimeMillis();
        
        // 1. 批量查询分类
        Set<Long> categoryIds = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ProductCategory> categoryMap = categoryIds.isEmpty() ? 
                Collections.emptyMap() : 
                categoryRepository.selectBatchIds(categoryIds)
                        .stream()
                        .collect(Collectors.toMap(ProductCategory::getId, c -> c, (a, b) -> a));

        // 2. 批量查询品牌
        Set<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Brand> brandMap = brandIds.isEmpty() ? 
                Collections.emptyMap() : 
                brandRepository.selectBatchIds(brandIds)
                        .stream()
                        .collect(Collectors.toMap(Brand::getId, b -> b, (a, b) -> a));

        // 3. 查询用户信息（如果 userId 不为 null，只查询一次）
        User user = null;
        if (userId != null) {
            user = userRepository.selectById(userId);
        }
        final User finalUser = user;

        // 4. 批量查询商品会员价配置（仅管理后台需要，userId 为 null）
        final Map<Long, List<ProductMemberPrice>> productMemberPriceMap;
        if (userId == null) {
            Set<Long> productIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                LambdaQueryWrapper<ProductMemberPrice> memberPriceWrapper = new LambdaQueryWrapper<>();
                memberPriceWrapper.in(ProductMemberPrice::getProductId, productIds);
                List<ProductMemberPrice> allMemberPrices = productMemberPriceRepository.selectList(memberPriceWrapper);
                productMemberPriceMap = allMemberPrices.stream()
                        .collect(Collectors.groupingBy(ProductMemberPrice::getProductId));
            } else {
                productMemberPriceMap = Collections.emptyMap();
            }
        } else {
            productMemberPriceMap = Collections.emptyMap();
        }

        long batchQueryTime = System.currentTimeMillis() - batchQueryStartTime;
        log.info("热门商品批量查询关联数据耗时: {}ms (分类: {}, 品牌: {}, 用户: {})", 
                batchQueryTime, categoryMap.size(), brandMap.size(), finalUser != null ? 1 : 0);

        // 转换为VO（使用批量查询的数据）
        long convertStartTime = System.currentTimeMillis();
        List<ProductVO> result = products.stream()
                .map(p -> {
                    ProductVO vo = convertToVOWithCache(p, finalUser, categoryMap, brandMap, productMemberPriceMap);
                    vo.setDescription(null); // 列表接口不返回description字段（双重保险）
                    return vo;
                })
                .toList();
        long convertTime = System.currentTimeMillis() - convertStartTime;
        log.info("热门商品VO转换耗时: {}ms", convertTime);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("热门商品查询总耗时: {}ms (查询: {}ms, 批量查询: {}ms, 转换: {}ms)", 
                totalTime, queryTime, batchQueryTime, convertTime);

        return result;
    }

    @Override
    public List<ProductVO> getRecommendProducts(Long categoryId, Long limit, Long userId) {
        long startTime = System.currentTimeMillis();
        
        Page<Product> page = new Page<>(1, limit);

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        // 获取该分类及其所有子分类的ID列表（支持查询父分类及其所有子分类的商品）
        List<Long> categoryIds = categoryService.getAllCategoryIdsIncludingChildren(categoryId);
        wrapper.in(Product::getCategoryId, categoryIds)
                .eq(Product::getStatus, 1) // 1=上架
                .orderByDesc(Product::getSalesCount);

        // 优化：排除 description 字段，提升查询性能（大字段）
        wrapper.select(Product::getId, Product::getProductCode, Product::getBarcode, Product::getUnit,
                Product::getProductName, Product::getCategoryId, Product::getBrandId,
                Product::getShippingTemplateId, Product::getMainImage, Product::getImages,
                Product::getBasePrice, Product::getSuggestedRetailPrice, Product::getMarketRetailPrice,
                Product::getMemberPrice, Product::getEnableMemberPrice,
                Product::getStock, Product::getWarningStock, Product::getWeight,
                Product::getStatus, Product::getSalesCount, Product::getEnableSpec,
                Product::getCreateTime, Product::getUpdateTime);
        // 明确不包含 description 字段

        long queryStartTime = System.currentTimeMillis();
        Page<Product> productPage = productRepository.selectPage(page, wrapper);
        long queryTime = System.currentTimeMillis() - queryStartTime;
        log.info("推荐商品查询耗时: {}ms, 查询到{}条记录", queryTime, productPage.getRecords().size());

        List<Product> products = productPage.getRecords();
        if (products.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查询优化：解决 N+1 查询问题
        long batchQueryStartTime = System.currentTimeMillis();
        
        // 1. 批量查询分类
        Set<Long> categoryIdsForMap = products.stream()
                .map(Product::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, ProductCategory> categoryMap = categoryIdsForMap.isEmpty() ? 
                Collections.emptyMap() : 
                categoryRepository.selectBatchIds(categoryIdsForMap)
                        .stream()
                        .collect(Collectors.toMap(ProductCategory::getId, c -> c, (a, b) -> a));

        // 2. 批量查询品牌
        Set<Long> brandIds = products.stream()
                .map(Product::getBrandId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, Brand> brandMap = brandIds.isEmpty() ? 
                Collections.emptyMap() : 
                brandRepository.selectBatchIds(brandIds)
                        .stream()
                        .collect(Collectors.toMap(Brand::getId, b -> b, (a, b) -> a));

        // 3. 查询用户信息（如果 userId 不为 null，只查询一次）
        User user = null;
        if (userId != null) {
            user = userRepository.selectById(userId);
        }
        final User finalUser = user;

        // 4. 批量查询商品会员价配置（仅管理后台需要，userId 为 null）
        final Map<Long, List<ProductMemberPrice>> productMemberPriceMap;
        if (userId == null) {
            Set<Long> productIds = products.stream()
                    .map(Product::getId)
                    .collect(Collectors.toSet());
            if (!productIds.isEmpty()) {
                LambdaQueryWrapper<ProductMemberPrice> memberPriceWrapper = new LambdaQueryWrapper<>();
                memberPriceWrapper.in(ProductMemberPrice::getProductId, productIds);
                List<ProductMemberPrice> allMemberPrices = productMemberPriceRepository.selectList(memberPriceWrapper);
                productMemberPriceMap = allMemberPrices.stream()
                        .collect(Collectors.groupingBy(ProductMemberPrice::getProductId));
            } else {
                productMemberPriceMap = Collections.emptyMap();
            }
        } else {
            productMemberPriceMap = Collections.emptyMap();
        }

        long batchQueryTime = System.currentTimeMillis() - batchQueryStartTime;
        log.info("推荐商品批量查询关联数据耗时: {}ms (分类: {}, 品牌: {}, 用户: {})", 
                batchQueryTime, categoryMap.size(), brandMap.size(), finalUser != null ? 1 : 0);

        // 转换为VO（使用批量查询的数据）
        long convertStartTime = System.currentTimeMillis();
        List<ProductVO> result = products.stream()
                .map(p -> {
                    ProductVO vo = convertToVOWithCache(p, finalUser, categoryMap, brandMap, productMemberPriceMap);
                    vo.setDescription(null); // 列表接口不返回description字段（双重保险）
                    return vo;
                })
                .toList();
        long convertTime = System.currentTimeMillis() - convertStartTime;
        log.info("推荐商品VO转换耗时: {}ms", convertTime);

        long totalTime = System.currentTimeMillis() - startTime;
        log.info("推荐商品查询总耗时: {}ms (查询: {}ms, 批量查询: {}ms, 转换: {}ms)", 
                totalTime, queryTime, batchQueryTime, convertTime);

        return result;
    }

    /**
     * 转换为VO（使用批量查询的缓存数据，优化性能）
     */
    private ProductVO convertToVOWithCache(Product product, User user,
                                          Map<Long, ProductCategory> categoryMap,
                                          Map<Long, Brand> brandMap,
                                          Map<Long, List<ProductMemberPrice>> productMemberPriceMap) {
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

        // 使用缓存的分类数据（避免 N+1 查询）
        ProductCategory category = categoryMap.get(product.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getCategoryName());
        }

        // 使用缓存的品牌数据（避免 N+1 查询）
        if (product.getBrandId() != null) {
            Brand brand = brandMap.get(product.getBrandId());
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

        // 使用缓存的用户信息（避免 N+1 查询）
        Integer isMember = 0;
        Long userId = user != null ? user.getId() : null;
        if (user != null && user.getIsMember() != null && user.getIsMember() == 1) {
            isMember = 1;
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

        // 1.1 加载商品会员价配置（管理后台编辑时使用，使用批量查询的数据）
        if (userId == null) { // 管理后台查询时userId为null
            try {
                List<ProductMemberPrice> productMemberPrices = productMemberPriceMap.get(product.getId());
                if (productMemberPrices != null && !productMemberPrices.isEmpty()) {
                    List<com.shoppingmall.vo.ProductMemberPriceVO> memberPriceVOs = productMemberPrices.stream()
                        .map(mp -> {
                            com.shoppingmall.vo.ProductMemberPriceVO mpVO = new com.shoppingmall.vo.ProductMemberPriceVO();
                            mpVO.setMemberLevelId(mp.getMemberLevelId());
                            mpVO.setMemberPrice(mp.getMemberPrice());
                            return mpVO;
                        })
                        .collect(Collectors.toList());
                    vo.setMemberPrices(memberPriceVOs);
                }
            } catch (Exception e) {
                log.error("加载商品会员价配置失败: productId={}", product.getId(), e);
            }
        }

        // 2. 获取 SKU 列表并处理 SKU 层级的会员价
        // 注意：SKU 查询仍然按需查询，因为可能涉及复杂的会员价计算逻辑
        // 如果性能仍有问题，可以进一步优化为批量查询
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
     * 转换为VO（保留原方法，用于其他场景）
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
