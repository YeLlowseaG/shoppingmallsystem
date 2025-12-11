package com.shoppingmall.service.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shoppingmall.common.exception.BusinessException;
import com.shoppingmall.common.util.StringUtil;
import com.shoppingmall.dto.StockDTO;
import com.shoppingmall.dto.StockQueryDTO;
import com.shoppingmall.entity.Product;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.service.admin.StockService;
import com.shoppingmall.vo.StockStatisticsVO;
import com.shoppingmall.vo.StockVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    @Override
    public Page<StockVO> getStockPage(Long current, Long size, StockQueryDTO queryDTO) {
        // 如果提供了商品编码或名称，先查询商品ID列表
        List<Long> productIds = null;
        if (StringUtil.isNotBlank(queryDTO.getProductCode()) || StringUtil.isNotBlank(queryDTO.getProductName())) {
            LambdaQueryWrapper<Product> productWrapper = new LambdaQueryWrapper<>();
            if (StringUtil.isNotBlank(queryDTO.getProductCode())) {
                productWrapper.like(Product::getProductCode, queryDTO.getProductCode());
            }
            if (StringUtil.isNotBlank(queryDTO.getProductName())) {
                productWrapper.like(Product::getProductName, queryDTO.getProductName());
            }
            List<Product> products = productRepository.selectList(productWrapper);
            productIds = products.stream().map(Product::getId).collect(Collectors.toList());
            
            // 如果没有找到匹配的商品，返回空结果
            if (productIds.isEmpty()) {
                Page<StockVO> emptyPage = new Page<>();
                emptyPage.setCurrent(current);
                emptyPage.setSize(size);
                emptyPage.setTotal(0);
                emptyPage.setRecords(new ArrayList<>());
                return emptyPage;
            }
        }

        Page<ProductStock> page = new Page<>(current, size);

        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();

        // 商品ID筛选
        if (queryDTO.getProductId() != null) {
            wrapper.eq(ProductStock::getProductId, queryDTO.getProductId());
        } else if (productIds != null && !productIds.isEmpty()) {
            // 使用商品ID列表筛选
            wrapper.in(ProductStock::getProductId, productIds);
        }

        // 预警筛选
        if (Boolean.TRUE.equals(queryDTO.getOnlyWarning())) {
            // 查询可用库存 <= 预警阈值的商品
            wrapper.apply("available_stock <= warning_threshold");
        }

        // 按更新时间倒序
        wrapper.orderByDesc(ProductStock::getUpdateTime);

        Page<ProductStock> stockPage = stockRepository.selectPage(page, wrapper);

        // 转换为VO并填充商品信息
        Page<StockVO> voPage = new Page<>();
        voPage.setCurrent(stockPage.getCurrent());
        voPage.setSize(stockPage.getSize());
        voPage.setTotal(stockPage.getTotal());
        voPage.setRecords(stockPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

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
        if (stockDTO.getWarningThreshold() != null) {
            stock.setWarningThreshold(stockDTO.getWarningThreshold());
        }

        if (stock.getId() == null) {
            stockRepository.insert(stock);
        } else {
            stockRepository.updateById(stock);
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
     * 转换为VO
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
        }

        return vo;
    }
}

