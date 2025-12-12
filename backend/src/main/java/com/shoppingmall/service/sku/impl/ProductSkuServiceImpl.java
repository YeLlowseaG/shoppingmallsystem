package com.shoppingmall.service.sku.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品SKU Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl implements ProductSkuService {
    
    private final ProductSkuRepository skuRepository;
    
    @Override
    @Transactional
    public Long createSku(ProductSkuDTO dto) {
        // 检查SKU编码是否重复
        if (existsBySkuCode(dto.getSkuCode())) {
            throw new RuntimeException("SKU编码已存在: " + dto.getSkuCode());
        }
        
        ProductSku entity = new ProductSku();
        BeanUtils.copyProperties(dto, entity);
        
        // 设置默认值
        if (entity.getWarningStock() == null) {
            entity.setWarningStock(0);
        }
        if (entity.getSalesCount() == null) {
            entity.setSalesCount(0);
        }
        if (entity.getStatus() == null) {
            entity.setStatus(1);
        }
        
        skuRepository.insert(entity);
        log.info("创建SKU成功，ID: {}, SKU编码: {}, 商品ID: {}", 
                entity.getId(), dto.getSkuCode(), dto.getProductId());
        return entity.getId();
    }
    
    @Override
    @Transactional
    public int batchCreateSkus(List<ProductSkuDTO> dtoList) {
        int successCount = 0;
        for (ProductSkuDTO dto : dtoList) {
            try {
                createSku(dto);
                successCount++;
            } catch (Exception e) {
                log.error("批量创建SKU失败，SKU编码: {}, 错误: {}", dto.getSkuCode(), e.getMessage());
            }
        }
        log.info("批量创建SKU完成，总数: {}, 成功: {}", dtoList.size(), successCount);
        return successCount;
    }
    
    @Override
    @Transactional
    public boolean updateSku(Long id, ProductSkuDTO dto) {
        ProductSku entity = skuRepository.selectById(id);
        if (entity == null) {
            log.warn("SKU不存在，ID: {}", id);
            return false;
        }
        
        // 如果SKU编码有变化，检查是否重复
        if (!entity.getSkuCode().equals(dto.getSkuCode()) && existsBySkuCode(dto.getSkuCode())) {
            throw new RuntimeException("SKU编码已存在: " + dto.getSkuCode());
        }
        
        BeanUtils.copyProperties(dto, entity);
        entity.setId(id);
        
        int result = skuRepository.updateById(entity);
        log.info("更新SKU成功，ID: {}, SKU编码: {}", id, dto.getSkuCode());
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean deleteSku(Long id) {
        ProductSku entity = skuRepository.selectById(id);
        if (entity == null) {
            log.warn("SKU不存在，ID: {}", id);
            return false;
        }
        
        int result = skuRepository.deleteById(id);
        log.info("删除SKU成功，ID: {}, SKU编码: {}", id, entity.getSkuCode());
        return result > 0;
    }
    
    @Override
    public List<ProductSkuVO> getSkusByProductId(Long productId) {
        List<ProductSku> skus = skuRepository.findByProductId(productId);
        return skus.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public ProductSkuVO getSkuById(Long id) {
        ProductSku entity = skuRepository.selectById(id);
        return entity != null ? convertToVO(entity) : null;
    }
    
    @Override
    public ProductSkuVO getSkuByCode(String skuCode) {
        ProductSku entity = skuRepository.findBySkuCode(skuCode);
        return entity != null ? convertToVO(entity) : null;
    }
    
    @Override
    public ProductSkuVO getSkuBySpecCombination(Long productId, String specCombination) {
        ProductSku entity = skuRepository.findByProductIdAndSpecCombination(productId, specCombination);
        return entity != null ? convertToVO(entity) : null;
    }
    
    @Override
    @Transactional
    public boolean updateSkuStock(Long skuId, Integer stock) {
        int result = skuRepository.updateStock(skuId, stock);
        if (result > 0) {
            log.info("更新SKU库存成功，SKU ID: {}, 库存: {}", skuId, stock);
        }
        return result > 0;
    }
    
    @Override
    @Transactional
    public boolean increaseSalesCount(Long skuId, Integer salesCount) {
        int result = skuRepository.increaseSalesCount(skuId, salesCount);
        if (result > 0) {
            log.info("增加SKU销量成功，SKU ID: {}, 销量增加: {}", skuId, salesCount);
        }
        return result > 0;
    }
    
    @Override
    public List<ProductSkuVO> getLowStockSkus() {
        List<ProductSku> skus = skuRepository.findLowStockSkus();
        return skus.stream().map(this::convertToVO).collect(Collectors.toList());
    }
    
    @Override
    public boolean existsBySkuCode(String skuCode) {
        if (!StringUtils.hasText(skuCode)) {
            return false;
        }
        ProductSku entity = skuRepository.findBySkuCode(skuCode);
        return entity != null;
    }
    
    /**
     * 实体转VO
     */
    private ProductSkuVO convertToVO(ProductSku entity) {
        ProductSkuVO vo = new ProductSkuVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}