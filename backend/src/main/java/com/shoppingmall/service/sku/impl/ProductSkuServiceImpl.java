package com.shoppingmall.service.sku.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shoppingmall.dto.ProductSkuDTO;
import com.shoppingmall.entity.ProductSku;
import com.shoppingmall.entity.ProductSpecKey;
import com.shoppingmall.entity.ProductSpecValue;
import com.shoppingmall.entity.ProductStock;
import com.shoppingmall.repository.product.ProductRepository;
import com.shoppingmall.repository.product.ProductStockRepository;
import com.shoppingmall.repository.sku.ProductSkuRepository;
import com.shoppingmall.repository.sku.ProductSpecKeyRepository;
import com.shoppingmall.repository.sku.ProductSpecValueRepository;
import com.shoppingmall.service.sku.ProductSkuService;
import com.shoppingmall.vo.ProductSkuVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商品SKU Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductSkuServiceImpl implements ProductSkuService {

    private final ProductSkuRepository skuRepository;
    private final ProductSpecKeyRepository specKeyRepository;
    private final ProductSpecValueRepository specValueRepository;
    private final ProductRepository productRepository;
    private final ProductStockRepository productStockRepository;
    private final ObjectMapper objectMapper;
    
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

        // 同步规格值到product_spec_value表
        syncSpecValues(dto.getProductId(), dto.getSpecCombination());

        // 更新商品总库存
        updateProductTotalStock(dto.getProductId());

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

        // 同步规格值到product_spec_value表
        syncSpecValues(dto.getProductId(), dto.getSpecCombination());

        // 更新商品总库存
        updateProductTotalStock(dto.getProductId());

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

        Long productId = entity.getProductId();
        int result = skuRepository.deleteById(id);
        log.info("删除SKU成功，ID: {}, SKU编码: {}", id, entity.getSkuCode());

        // 更新商品总库存
        updateProductTotalStock(productId);

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
        // 先获取SKU信息以获取productId
        ProductSku sku = skuRepository.selectById(skuId);
        if (sku == null) {
            log.warn("SKU不存在，ID: {}", skuId);
            return false;
        }

        int result = skuRepository.updateStock(skuId, stock);
        if (result > 0) {
            log.info("更新SKU库存成功，SKU ID: {}, 库存: {}", skuId, stock);
            // 更新商品总库存
            updateProductTotalStock(sku.getProductId());
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
     * 同步规格值到product_spec_value表
     * 解析SKU的specCombination JSON，提取规格值并保存到product_spec_value表
     */
    private void syncSpecValues(Long productId, String specCombination) {
        if (!StringUtils.hasText(specCombination)) {
            return;
        }

        try {
            // 解析specCombination JSON，格式：{"颜色":"白色","尺寸":"L"}
            Map<String, String> specMap = objectMapper.readValue(
                    specCombination,
                    new TypeReference<Map<String, String>>() {}
            );

            // 获取该商品的所有规格属性
            List<ProductSpecKey> specKeys = specKeyRepository.findByProductId(productId);

            // 遍历规格组合中的每个键值对
            for (Map.Entry<String, String> entry : specMap.entrySet()) {
                String specName = entry.getKey();    // 如：颜色
                String specValue = entry.getValue(); // 如：白色

                // 查找对应的spec_key_id
                ProductSpecKey specKey = specKeys.stream()
                        .filter(sk -> sk.getSpecName().equals(specName))
                        .findFirst()
                        .orElse(null);

                // 如果规格属性不存在，则自动创建
                if (specKey == null) {
                    specKey = new ProductSpecKey();
                    specKey.setProductId(productId);
                    specKey.setSpecName(specName);
                    specKey.setSortOrder(specKeys.size());
                    specKeyRepository.insert(specKey);
                    specKeys.add(specKey); // 添加到列表中，避免重复创建
                    log.info("自动同步规格属性：商品ID={}, 规格名={}", productId, specName);
                }

                // 检查该规格值是否已存在
                LambdaQueryWrapper<ProductSpecValue> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ProductSpecValue::getSpecKeyId, specKey.getId())
                       .eq(ProductSpecValue::getSpecValue, specValue);
                ProductSpecValue existingValue = specValueRepository.selectOne(wrapper);

                // 如果不存在，则插入
                if (existingValue == null) {
                    ProductSpecValue newValue = new ProductSpecValue();
                    newValue.setSpecKeyId(specKey.getId());
                    newValue.setSpecValue(specValue);
                    newValue.setSortOrder(0);
                    specValueRepository.insert(newValue);
                    log.info("自动同步规格值：商品ID={}, 规格名={}, 规格值={}",
                            productId, specName, specValue);
                }
            }
        } catch (Exception e) {
            log.error("同步规格值失败：商品ID={}, specCombination={}",
                    productId, specCombination, e);
        }
    }

    /**
     * 更新商品总库存
     * 计算该商品所有SKU的库存总和，同步更新到product表和product_stock表
     *
     * 核心逻辑：可用库存 = 总库存 - 锁定库存
     */
    private void updateProductTotalStock(Long productId) {
        // 查询该商品的所有SKU
        List<ProductSku> skus = skuRepository.findByProductId(productId);

        // 计算总库存
        int totalStock = skus.stream()
                .mapToInt(sku -> sku.getStock() != null ? sku.getStock() : 0)
                .sum();

        // 更新商品表的库存
        var product = productRepository.selectById(productId);
        if (product != null) {
            product.setStock(totalStock);
            productRepository.updateById(product);
            log.info("更新商品总库存成功：商品ID={}, 总库存={}", productId, totalStock);
        }

        // 同步更新product_stock表
        LambdaQueryWrapper<ProductStock> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ProductStock::getProductId, productId);
        ProductStock productStock = productStockRepository.selectOne(wrapper);

        if (productStock == null) {
            // 创建新的库存记录
            productStock = new ProductStock();
            productStock.setProductId(productId);
            productStock.setTotalStock(totalStock);
            productStock.setLockedStock(0);
            productStock.setAvailableStock(totalStock); // 总库存 - 锁定库存(0)
            productStock.setWarningThreshold(10);
            productStockRepository.insert(productStock);
            log.info("创建商品库存记录成功：商品ID={}, 总库存={}, 可用库存={}",
                    productId, totalStock, totalStock);
        } else {
            // 更新现有记录，保留锁定库存
            int lockedStock = productStock.getLockedStock() != null ? productStock.getLockedStock() : 0;

            productStock.setTotalStock(totalStock);
            // 核心公式：可用库存 = 总库存 - 锁定库存
            int availableStock = totalStock - lockedStock;
            if (availableStock < 0) {
                availableStock = 0;
            }
            productStock.setAvailableStock(availableStock);

            productStockRepository.updateById(productStock);
            log.info("同步更新product_stock表成功：商品ID={}, 总库存={}, 锁定库存={}, 可用库存={}",
                    productId, totalStock, lockedStock, availableStock);
        }
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