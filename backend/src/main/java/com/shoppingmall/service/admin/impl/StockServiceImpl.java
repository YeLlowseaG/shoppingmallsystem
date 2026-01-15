package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.StockDTO;
import com.shoppingmall.dto.StockQueryDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.vo.StockStatisticsVO;
import com.shoppingmall.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.Resource;
import com.shoppingmall.service.erp.JushuitanInventoryService;
import com.shoppingmall.service.erp.JushuitanConfigService;
import com.shoppingmall.vo.JushuitanConfigVO;

/**
 * 库存管理服务实现类（管理后台使用）
 *
 * @author ShoppingMall Team
 * @date 2025-12-10
 */
@Slf4j
@Service("adminStockService")
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final ObjectMapper objectMapper;
    
    @Resource
    private JushuitanInventoryService jushuitanInventoryService;
    
    @Resource
    private JushuitanConfigService jushuitanConfigService;

    @Override
    public Page<StockVO> getStockPage(Long current, Long size, StockQueryDTO queryDTO) {
        // 从商品表查询所有商品（包括所有状态）
        LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
        
        // 商品编码筛选
        if (StringUtil.isNotBlank(queryDTO.getProductCode())) {
            productWrapper.like(Product::getProductCode, queryDTO.getProductCode());
        }
        
        // 商品名称筛选
        if (StringUtil.isNotBlank(queryDTO.getProductName())) {
            productWrapper.like(Product::getProductName, queryDTO.getProductName());
        }
        
        // 商品ID筛选
        if (queryDTO.getProductId() != null) {
            productWrapper.eq(Product::getId, queryDTO.getProductId());
        }
        
        // 商品状态筛选
        if (queryDTO.getProductStatus() != null) {
            productWrapper.eq(Product::getStatus, queryDTO.getProductStatus());
        }
        
        // 按创建时间倒序
        productWrapper.orderByDesc(Product::getCreateTime);
        
        // 分页查询商品
        Page<Product> productPage = new Page<>(current, size);
        Page<Product> products = productRepository.selectPage(productPage, productWrapper);
        
        if (products.getRecords().isEmpty()) {
            Page<StockVO> emptyPage = new Page<>();
            emptyPage.setCurrent(current);
            emptyPage.setSize(size);
            emptyPage.setTotal(0);
            emptyPage.setRecords(new ArrayList<>());
            return emptyPage;
        }
        
        // 获取商品ID列表
        List<Long> productIds = products.getRecords().stream()
                .map(Product::getId)
                .collect(Collectors.toList());

        // 批量查询SKU信息，用于判断是否启用了规格
        LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.in(ProductSku::getProductId, productIds);
        skuWrapper.eq(ProductSku::getStatus, 1); // 只查询启用的SKU
        List<ProductSku> allSkus = productSkuRepository.selectList(skuWrapper);

        // 按商品ID分组SKU
        Map<Long, List<ProductSku>> skusByProduct = allSkus.stream()
                .collect(Collectors.groupingBy(ProductSku::getProductId));

        // 已移除product_stock表的查询，统一使用product.stock和product_sku.stock
        
        // 预警筛选（如果启用）
        List<Product> filteredProducts = products.getRecords();
        if (Boolean.TRUE.equals(queryDTO.getOnlyWarning())) {
            filteredProducts = products.getRecords().stream()
                    .filter(product -> {
                        List<ProductSku> productSkus = skusByProduct.get(product.getId());

                        // 如果有SKU，检查SKU汇总库存是否预警
                        if (productSkus != null && !productSkus.isEmpty()) {
                            int totalStock = productSkus.stream()
                                    .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                                    .sum();
                            int warningStock = productSkus.stream()
                                    .mapToInt(sku -> sku.getWarningStock() != null ? sku.getWarningStock() : 10)
                                    .min()
                                    .orElse(10);
                            return totalStock <= warningStock;
                        }

                        // 检查商品表的库存和预警阈值
                        Integer productStock = product.getStock();
                        Integer productWarning = product.getWarningStock();
                        if (productStock != null && productWarning != null) {
                            return productStock <= productWarning;
                        }

                        return false;
                    })
                    .collect(Collectors.toList());
        }
        
        // 转换为VO（不再使用product_stock表）
        List<StockVO> voList = filteredProducts.stream()
                .map(product -> {
                    List<ProductSku> productSkus = skusByProduct.get(product.getId());
                    return convertToVO(product, productSkus);
                })
                .collect(Collectors.toList());
        
        // 构建分页结果
        Page<StockVO> voPage = new Page<>();
        voPage.setCurrent(products.getCurrent());
        voPage.setSize(products.getSize());
        voPage.setTotal(products.getTotal());
        voPage.setRecords(voList);
        
        return voPage;
    }

    @Override
    public StockVO getStockByProductId(Long productId) {
        // 查询商品信息
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 查询SKU信息
        LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
        skuWrapper.eq(ProductSku::getProductId, productId);
        skuWrapper.eq(ProductSku::getStatus, 1);
        List<ProductSku> skus = productSkuRepository.selectList(skuWrapper);
        
        return convertToVO(product, skus);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(StockDTO stockDTO) {
        // 查询商品信息
        Product product = productRepository.selectById(stockDTO.getProductId());
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }

        // 获取当前库存（从product表）
        int currentStock = product.getStock() != null ? product.getStock() : 0;
        
        // 调整库存
        int newTotalStock = currentStock + stockDTO.getAdjustQuantity();
        if (newTotalStock < 0) {
            throw new BusinessException(400, "调整后库存不能为负数");
        }

        // 更新商品库存
        product.setStock(newTotalStock);
        
        // 更新预警阈值（如果提供了）
        boolean warningThresholdUpdated = false;
        if (stockDTO.getWarningThreshold() != null) {
            product.setWarningStock(stockDTO.getWarningThreshold());
            warningThresholdUpdated = true;
        }
        
        productRepository.updateById(product);

        log.info("库存调整成功，商品ID: {}, 调整数量: {}, 原因: {}", 
                stockDTO.getProductId(), stockDTO.getAdjustQuantity(), stockDTO.getReason());
        
        // 自动同步库存到聚水潭ERP
        try {
            JushuitanConfigVO config = jushuitanConfigService.getEnabledConfig();
            if (config != null && config.getAutoSyncProduct() == 1) {
                log.info("开始自动同步商品{}库存到聚水潭ERP", stockDTO.getProductId());
                jushuitanInventoryService.syncInventory(stockDTO.getProductId());
                log.info("商品{}库存自动同步到聚水潭ERP成功", stockDTO.getProductId());
            }
        } catch (Exception e) {
            log.error("商品{}库存自动同步到聚水潭ERP失败: {}", stockDTO.getProductId(), e.getMessage(), e);
            // 不影响库存调整的主流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarningThreshold(Long productId, Integer warningThreshold) {
        // 直接更新product表的warning_stock字段（不再使用product_stock表）
        Product product = productRepository.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        product.setWarningStock(warningThreshold);
        productRepository.updateById(product);
        log.info("更新商品预警阈值成功，商品ID: {}, 预警阈值: {}", productId, warningThreshold);
    }

    @Override
    public Page<StockVO> getWarningStockPage(Long current, Long size) {
        StockQueryDTO queryDTO = new StockQueryDTO();
        queryDTO.setOnlyWarning(true);
        return getStockPage(current, size, queryDTO);
    }

    @Override
    public StockStatisticsVO getStockStatistics() {
        StockStatisticsVO statistics = new StockStatisticsVO();

        // 查询所有商品（不再使用product_stock表）
        List<Product> allProducts = productRepository.selectList(null);

        // 统计总库存（从product表和product_sku表汇总）
        long totalStock = 0;
        long totalAvailableStock = 0;
        long warningCount = 0;
        long outOfStockCount = 0;
        
        for (Product product : allProducts) {
            // 查询SKU
            LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
            skuWrapper.eq(ProductSku::getProductId, product.getId());
            skuWrapper.eq(ProductSku::getStatus, 1);
            List<ProductSku> skus = productSkuRepository.selectList(skuWrapper);
            
            int productStock = 0;
            if (!skus.isEmpty()) {
                // 有SKU，汇总SKU库存
                productStock = skus.stream()
                        .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                        .sum();
            } else {
                // 无SKU，使用商品库存
                productStock = product.getStock() != null ? product.getStock() : 0;
            }
            
            totalStock += productStock;
            totalAvailableStock += productStock; // 可用库存等于总库存（锁定库存通过订单状态计算）
            
            // 判断是否预警
            int warningThreshold = product.getWarningStock() != null ? product.getWarningStock() : 10;
            if (productStock <= warningThreshold) {
                warningCount++;
            }
            
            // 判断是否缺货
            if (productStock == 0) {
                outOfStockCount++;
            }
        }

        // 统计锁定库存（通过查询未支付订单计算）
        // 注意：这里暂时设为0，如果需要显示锁定库存，需要注入StockUtil并调用相关方法
        long totalLockedStock = 0;

        statistics.setTotalProducts((long) allProducts.size());
        statistics.setTotalStock(totalStock);
        statistics.setTotalAvailableStock(totalAvailableStock);
        statistics.setTotalLockedStock(totalLockedStock);
        statistics.setWarningProductCount(warningCount);
        statistics.setOutOfStockCount(outOfStockCount);

        return statistics;
    }

    /**
     * 转换为VO（支持SKU库存汇总和详情）
     * 不再使用product_stock表，统一使用product.stock和product_sku.stock
     */
    private StockVO convertToVO(Product product, List<ProductSku> productSkus) {
        StockVO vo = new StockVO();

        // 设置商品信息
        vo.setProductId(product.getId());
        vo.setProductCode(product.getProductCode());
        vo.setProductName(product.getProductName());
        vo.setMainImage(product.getMainImage());
        vo.setProductStatus(product.getStatus());

        // 如果有SKU，优先使用SKU汇总库存
        if (productSkus != null && !productSkus.isEmpty()) {
            vo.setEnableSpec(true);

            // 汇总所有SKU的库存
            int totalStock = productSkus.stream()
                    .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                    .sum();

            // 汇总所有SKU的警戒库存（取最小值）
            int warningStock = productSkus.stream()
                    .mapToInt(sku -> sku.getWarningStock() != null ? sku.getWarningStock() : 10)
                    .min()
                    .orElse(10);

            vo.setId(null); // 不再使用product_stock表的ID
            vo.setAvailableStock(totalStock);
            vo.setLockedStock(0); // 锁定库存通过查询未支付订单计算，这里不显示
            vo.setTotalStock(totalStock);
            vo.setWarningThreshold(warningStock);
            vo.setUpdateTime(product.getUpdateTime());

            // 判断是否预警
            vo.setIsWarning(totalStock <= warningStock);

            // 填充SKU库存列表
            List<StockVO.SkuStockVO> skuStockList = new ArrayList<>();
            for (ProductSku sku : productSkus) {
                StockVO.SkuStockVO skuStockVO = new StockVO.SkuStockVO();
                skuStockVO.setSkuId(sku.getId());
                skuStockVO.setSkuCode(sku.getSkuCode());
                skuStockVO.setSpecCombination(sku.getSpecCombination());
                skuStockVO.setSpecText(parseSpecText(sku.getSpecCombination()));
                skuStockVO.setStock(sku.getStock() != null ? sku.getStock() : 0);
                skuStockVO.setWarningStock(sku.getWarningStock() != null ? sku.getWarningStock() : 10);
                skuStockVO.setIsWarning(skuStockVO.getStock() <= skuStockVO.getWarningStock());
                skuStockList.add(skuStockVO);
            }
            vo.setSkuStockList(skuStockList);
        } else {
            // 无SKU商品，使用product表的库存
            vo.setEnableSpec(false);
            vo.setId(null);
            int totalStock = product.getStock() != null ? product.getStock() : 0;
            vo.setTotalStock(totalStock);
            vo.setAvailableStock(totalStock); // 可用库存等于总库存（锁定库存通过订单状态计算）
            vo.setLockedStock(0); // 锁定库存通过查询未支付订单计算，这里不显示
            vo.setWarningThreshold(product.getWarningStock() != null ? product.getWarningStock() : 10);
            vo.setUpdateTime(product.getUpdateTime());

            // 判断是否预警
            int warningThreshold = product.getWarningStock() != null ? product.getWarningStock() : 10;
            vo.setIsWarning(totalStock <= warningThreshold);
        }

        return vo;
    }

    /**
     * 解析规格组合JSON为显示文本
     * 如：{"颜色":"红色","尺寸":"L"} -> "红色/L"
     */
    private String parseSpecText(String specCombination) {
        if (specCombination == null || specCombination.isEmpty()) {
            return "";
        }
        try {
            Map<String, String> specMap = objectMapper.readValue(
                    specCombination,
                    new TypeReference<Map<String, String>>() {}
            );
            return String.join("/", specMap.values());
        } catch (Exception e) {
            log.warn("解析规格组合失败: {}", specCombination, e);
            return specCombination;
        }
    }

    // 已移除convertToVO(ProductStock)方法，不再使用product_stock表

    // 已移除syncProductStock和syncProductWarningStock方法，不再使用product_stock表
    // 库存和预警阈值直接存储在product表中

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductTotalStock(Long productId, Integer totalStock) {
        try {
            // 直接更新product表的stock字段（不再使用product_stock表）
            Product product = productRepository.selectById(productId);
            if (product != null) {
                product.setStock(totalStock);
                productRepository.updateById(product);
                log.info("更新商品库存成功，商品ID: {}, 库存: {}", productId, totalStock);
            } else {
                log.warn("商品不存在，无法更新库存: productId={}", productId);
            }
            
            // 注意：不再同步SKU库存，因为：
            // 1. SKU库存应该由用户明确设置
            // 2. 商品总库存应该由所有SKU库存的总和计算得出（ProductSkuServiceImpl.updateProductTotalStock()）
            // 3. 不应该用商品总库存来覆盖用户设置的SKU库存
            
        } catch (Exception e) {
            log.error("更新商品库存失败，商品ID: {}, 库存: {}", productId, totalStock, e);
            throw new RuntimeException("更新商品库存失败", e);
        }
    }

    /**
     * 同步SKU库存
     * 当商品有SKU时，将商品总库存同步到所有SKU
     * 
     * @param productId 商品ID
     * @param totalStock 总库存
     */
    private void syncProductSkuStock(Long productId, Integer totalStock) {
        try {
            // 查询商品的所有SKU
            LambdaQueryWrapper<ProductSku> skuWrapper = new LambdaQueryWrapper<>();
            skuWrapper.eq(ProductSku::getProductId, productId);
            List<ProductSku> skuList = productSkuRepository.selectList(skuWrapper);
            
            if (!skuList.isEmpty()) {
                // 如果只有一个SKU，直接设置为总库存
                if (skuList.size() == 1) {
                    ProductSku sku = skuList.get(0);
                    sku.setStock(totalStock);
                    productSkuRepository.updateById(sku);
                    log.info("同步SKU库存成功，商品ID: {}, SKU ID: {}, 库存: {}", productId, sku.getId(), totalStock);
                } else {
                    // 多个SKU时，平均分配库存
                    int stockPerSku = totalStock / skuList.size();
                    int remainingStock = totalStock % skuList.size();
                    
                    for (int i = 0; i < skuList.size(); i++) {
                        ProductSku sku = skuList.get(i);
                        int skuStock = stockPerSku + (i < remainingStock ? 1 : 0);
                        sku.setStock(skuStock);
                        productSkuRepository.updateById(sku);
                        log.info("同步SKU库存成功，商品ID: {}, SKU ID: {}, 库存: {}", productId, sku.getId(), skuStock);
                    }
                }
            }
        } catch (Exception e) {
            log.error("同步SKU库存失败，商品ID: {}, 库存: {}", productId, totalStock, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
}

