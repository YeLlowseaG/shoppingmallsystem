package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.StockDTO;
import com.shoppingmall.dto.StockQueryDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.vo.StockStatisticsVO;
import com.shoppingmall.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final ProductStockRepository stockRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

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
        
        // 批量查询库存信息
        LambdaQueryWrapper<ProductStock> stockWrapper = new LambdaQueryWrapper<>();
        stockWrapper.in(ProductStock::getProductId, productIds);
        List<ProductStock> stocks = stockRepository.selectList(stockWrapper);
        
        // 创建库存Map，方便查找
        Map<Long, ProductStock> stockMap = stocks.stream()
                .collect(Collectors.toMap(ProductStock::getProductId, stock -> stock));
        
        // 预警筛选（如果启用）
        List<Product> filteredProducts = products.getRecords();
        if (Boolean.TRUE.equals(queryDTO.getOnlyWarning())) {
            filteredProducts = products.getRecords().stream()
                    .filter(product -> {
                        ProductStock stock = stockMap.get(product.getId());
                        if (stock == null) {
                            return false; // 没有库存记录的商品不预警
                        }
                        Integer available = stock.getAvailableStock();
                        Integer threshold = stock.getWarningThreshold();
                        return available != null && threshold != null && available <= threshold;
                    })
                    .collect(Collectors.toList());
        }
        
        // 转换为VO
        List<StockVO> voList = filteredProducts.stream()
                .map(product -> {
                    ProductStock stock = stockMap.get(product.getId());
                    return convertToVO(product, stock);
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
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId);
        ProductStock stock = stockRepository.selectOne(wrapper);
        
        if (stock == null) {
            throw new BusinessException(404, "库存信息不存在");
        }
        
        return convertToVO(stock);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustStock(StockDTO stockDTO) {
        // 查询库存信息
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, stockDTO.getProductId());
        ProductStock stock = stockRepository.selectOne(wrapper);

        if (stock == null) {
            // 如果库存记录不存在，创建新记录
            stock = new ProductStock();
            stock.setProductId(stockDTO.getProductId());
            stock.setAvailableStock(0);
            stock.setLockedStock(0);
            stock.setTotalStock(0);
            stock.setWarningThreshold(stockDTO.getWarningThreshold() != null ? stockDTO.getWarningThreshold() : 10);
        }

        // 调整库存
        int newTotalStock = stock.getTotalStock() + stockDTO.getAdjustQuantity();
        if (newTotalStock < 0) {
            throw new BusinessException(400, "调整后库存不能为负数");
        }

        stock.setTotalStock(newTotalStock);
        // 可用库存 = 总库存 - 锁定库存
        stock.setAvailableStock(newTotalStock - stock.getLockedStock());

        // 更新预警阈值（如果提供了）
        boolean warningThresholdUpdated = false;
        if (stockDTO.getWarningThreshold() != null) {
            stock.setWarningThreshold(stockDTO.getWarningThreshold());
            warningThresholdUpdated = true;
        }

        if (stock.getId() == null) {
            stockRepository.insert(stock);
        } else {
            stockRepository.updateById(stock);
        }

        // 同步更新 product 表的 stock 字段
        syncProductStock(stockDTO.getProductId(), newTotalStock);
        
        // 如果更新了预警阈值，同步更新 product 表的 warning_stock 字段
        if (warningThresholdUpdated) {
            syncProductWarningStock(stockDTO.getProductId(), stock.getWarningThreshold());
        }

        log.info("库存调整成功，商品ID: {}, 调整数量: {}, 原因: {}", 
                stockDTO.getProductId(), stockDTO.getAdjustQuantity(), stockDTO.getReason());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarningThreshold(Long productId, Integer warningThreshold) {
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId);
        ProductStock stock = stockRepository.selectOne(wrapper);

        if (stock == null) {
            throw new BusinessException(404, "库存信息不存在");
        }

        stock.setWarningThreshold(warningThreshold);
        stockRepository.updateById(stock);
        
        // 同步更新 product 表的 warning_stock 字段
        // 以 product_stock.warning_threshold 为权威数据源
        syncProductWarningStock(productId, warningThreshold);
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

        // 查询所有库存记录
        List<ProductStock> allStocks = stockRepository.selectList(null);

        // 统计总库存
        long totalStock = allStocks.stream()
                .mapToLong(stock -> stock.getTotalStock() != null ? stock.getTotalStock() : 0)
                .sum();

        // 统计可用库存
        long totalAvailableStock = allStocks.stream()
                .mapToLong(stock -> stock.getAvailableStock() != null ? stock.getAvailableStock() : 0)
                .sum();

        // 统计锁定库存
        long totalLockedStock = allStocks.stream()
                .mapToLong(stock -> stock.getLockedStock() != null ? stock.getLockedStock() : 0)
                .sum();

        // 统计预警商品数量
        long warningCount = allStocks.stream()
                .filter(stock -> {
                    Integer available = stock.getAvailableStock();
                    Integer threshold = stock.getWarningThreshold();
                    return available != null && threshold != null && available <= threshold;
                })
                .count();

        // 统计缺货商品数量
        long outOfStockCount = allStocks.stream()
                .filter(stock -> {
                    Integer available = stock.getAvailableStock();
                    return available == null || available == 0;
                })
                .count();

        // 统计商品总数（有库存记录的商品数）
        long totalProducts = allStocks.size();

        statistics.setTotalProducts(totalProducts);
        statistics.setTotalStock(totalStock);
        statistics.setTotalAvailableStock(totalAvailableStock);
        statistics.setTotalLockedStock(totalLockedStock);
        statistics.setWarningProductCount(warningCount);
        statistics.setOutOfStockCount(outOfStockCount);

        return statistics;
    }

    /**
     * 转换为VO（支持商品和库存信息）
     */
    private StockVO convertToVO(Product product, ProductStock stock) {
        StockVO vo = new StockVO();
        
        // 设置商品信息
        vo.setProductId(product.getId());
        vo.setProductCode(product.getProductCode());
        vo.setProductName(product.getProductName());
        vo.setMainImage(product.getMainImage());
        vo.setProductStatus(product.getStatus());
        
        // 设置库存信息（如果存在）
        if (stock != null) {
            vo.setId(stock.getId());
            vo.setAvailableStock(stock.getAvailableStock() != null ? stock.getAvailableStock() : 0);
            vo.setLockedStock(stock.getLockedStock() != null ? stock.getLockedStock() : 0);
            vo.setTotalStock(stock.getTotalStock() != null ? stock.getTotalStock() : 0);
            vo.setWarningThreshold(stock.getWarningThreshold() != null ? stock.getWarningThreshold() : 10);
            vo.setUpdateTime(stock.getUpdateTime());
            
            // 判断是否预警
            if (stock.getAvailableStock() != null && stock.getWarningThreshold() != null) {
                vo.setIsWarning(stock.getAvailableStock() <= stock.getWarningThreshold());
            } else {
                vo.setIsWarning(false);
            }
        } else {
            // 没有库存记录，使用默认值
            vo.setId(null);
            vo.setAvailableStock(0);
            vo.setLockedStock(0);
            vo.setTotalStock(0);
            vo.setWarningThreshold(10);
            vo.setUpdateTime(product.getUpdateTime());
            vo.setIsWarning(false);
        }
        
        return vo;
    }

    /**
     * 转换为VO（兼容旧方法）
     */
    private StockVO convertToVO(ProductStock stock) {
        StockVO vo = new StockVO();
        vo.setId(stock.getId());
        vo.setProductId(stock.getProductId());
        vo.setAvailableStock(stock.getAvailableStock());
        vo.setLockedStock(stock.getLockedStock());
        vo.setTotalStock(stock.getTotalStock());
        vo.setWarningThreshold(stock.getWarningThreshold());
        vo.setUpdateTime(stock.getUpdateTime());

        // 判断是否预警
        if (stock.getAvailableStock() != null && stock.getWarningThreshold() != null) {
            vo.setIsWarning(stock.getAvailableStock() <= stock.getWarningThreshold());
        } else {
            vo.setIsWarning(false);
        }

        // 查询商品信息
        Product product = productRepository.selectById(stock.getProductId());
        if (product != null) {
            vo.setProductCode(product.getProductCode());
            vo.setProductName(product.getProductName());
            vo.setMainImage(product.getMainImage());
            vo.setProductStatus(product.getStatus());
        }

        return vo;
    }

    /**
     * 同步 product 表的库存字段
     * 以 product_stock.total_stock 为权威数据源，同步更新 product.stock
     * 
     * @param productId 商品ID
     * @param totalStock 总库存
     */
    private void syncProductStock(Long productId, Integer totalStock) {
        try {
            Product product = productRepository.selectById(productId);
            if (product != null) {
                product.setStock(totalStock);
                productRepository.updateById(product);
                log.debug("同步商品库存成功，商品ID: {}, 库存: {}", productId, totalStock);
            }
        } catch (Exception e) {
            log.error("同步商品库存失败，商品ID: {}, 库存: {}", productId, totalStock, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    /**
     * 同步 product 表的预警库存字段
     * 以 product_stock.warning_threshold 为权威数据源，同步更新 product.warning_stock
     * 
     * @param productId 商品ID
     * @param warningThreshold 预警阈值
     */
    private void syncProductWarningStock(Long productId, Integer warningThreshold) {
        try {
            Product product = productRepository.selectById(productId);
            if (product != null) {
                product.setWarningStock(warningThreshold);
                productRepository.updateById(product);
                log.debug("同步商品预警库存成功，商品ID: {}, 预警阈值: {}", productId, warningThreshold);
            }
        } catch (Exception e) {
            log.error("同步商品预警库存失败，商品ID: {}, 预警阈值: {}", productId, warningThreshold, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductTotalStock(Long productId, Integer totalStock) {
        try {
            LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ProductStock::getProductId, productId);
            ProductStock stock = stockRepository.selectOne(wrapper);
            
            if (stock == null) {
                // 创建新的库存记录
                stock = new ProductStock();
                stock.setProductId(productId);
                stock.setTotalStock(totalStock);
                stock.setAvailableStock(totalStock);
                stock.setLockedStock(0);
                stock.setWarningThreshold(10); // 默认预警阈值
                stockRepository.insert(stock);
                log.info("创建商品库存记录成功，商品ID: {}, 库存: {}", productId, totalStock);
            } else {
                // 更新现有记录
                int oldTotalStock = stock.getTotalStock() != null ? stock.getTotalStock() : 0;
                int stockDifference = totalStock - oldTotalStock;
                
                stock.setTotalStock(totalStock);
                
                // 调整可用库存：新可用库存 = 原可用库存 + 库存差值
                int oldAvailableStock = stock.getAvailableStock() != null ? stock.getAvailableStock() : 0;
                int newAvailableStock = oldAvailableStock + stockDifference;
                if (newAvailableStock < 0) {
                    newAvailableStock = 0;
                }
                stock.setAvailableStock(newAvailableStock);
                
                stockRepository.updateById(stock);
                log.info("更新商品库存记录成功，商品ID: {}, 新库存: {}, 库存变化: {}", productId, totalStock, stockDifference);
            }
            
            // 同步更新Product表的库存字段（单向同步，避免冲突）
            syncProductStock(productId, totalStock);
            
            // 同步预警阈值
            if (stock.getWarningThreshold() != null) {
                syncProductWarningStock(productId, stock.getWarningThreshold());
            }
            
            // 同步SKU库存（当商品有SKU时，将商品总库存分配给SKU）
            syncProductSkuStock(productId, totalStock);
            
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

